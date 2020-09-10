package ognjenj.charon.acct.runner;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import inet.ipaddr.ipv4.IPv4Address;
import ognjenj.charon.acct.exceptions.SharedSecretMissingException;
import ognjenj.charon.acct.network.RadClient;
import ognjenj.charon.acct.ovpn.ClientEnvironmentInfo;
import ognjenj.charon.acct.ovpn.ClientSession;
import ognjenj.charon.acct.ovpn.ClientSessionStore;
import ognjenj.charon.acct.radius.RadAttribute;
import ognjenj.charon.acct.radius.RadAttributeType;
import ognjenj.charon.acct.radius.RadPacket;
import ognjenj.charon.acct.util.PasswordUtil;

public class InternalPluginServerThread extends Thread {
	private final Socket clientSocket;
	private final AtomicBoolean acceptConnections;
	private final AtomicBoolean runAccountingThread;
	private final PluginConfiguration config;
	private final ClientSessionStore sessionStore = ClientSessionStore.getInstance();
	private InputStreamReader reader;
	private OutputStreamWriter writer;

	public InternalPluginServerThread(Socket clientSocket, PluginConfiguration config, AtomicBoolean acceptConnections,
			AtomicBoolean runAccountingThread) {
		this.clientSocket = clientSocket;
		this.acceptConnections = acceptConnections;
		this.runAccountingThread = runAccountingThread;
		this.config = config;
		try {
			/*
			 * this.writer = new PrintWriter( new BufferedWriter(new
			 * OutputStreamWriter(this.clientSocket.getOutputStream())), true); this.reader
			 * = new BufferedReader(new
			 * InputStreamReader(this.clientSocket.getInputStream()));
			 */
			this.writer = new OutputStreamWriter(this.clientSocket.getOutputStream());
			this.reader = new InputStreamReader(this.clientSocket.getInputStream());
			start();

		} catch (IOException ex) {
			config.getLogger().error("Error opening stream reader/writer.", ex);
		}
	}

	@Override
	public synchronized void run() {
		try {
			char[] buffer = new char[4096];
			int messageLength = reader.read(buffer, 0, buffer.length);
			if (messageLength > 0) {
				String fullMessage = new String(buffer, 0, messageLength);
				String acceptedMessage = validateSignatureAndExtractMessage(fullMessage);
				if (acceptedMessage != null) {
					// the signature is valid
					if (acceptedMessage.toLowerCase().equals("exit")) {
						acceptConnections.set(false);
						runAccountingThread.set(false);
					} else if (acceptedMessage.startsWith("START#")) {
						String[] parameters = acceptedMessage.split("#");
						ClientEnvironmentInfo clientInfo = ClientEnvironmentInfo.parseEnvironmentData(parameters);
						File controlFile = new File(clientInfo.getEnvironmentMap()
								.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.CONTROL_FILE));
						PrintWriter controlFileWriter = new PrintWriter(
								new BufferedWriter(new FileWriter(controlFile, false)), true);
						boolean acceptClient = false;
						try {
							String username = clientInfo.getEnvironmentMap()
									.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.USERNAME);
							RadPacket accessRequest = RadPacket.RadPacketBuilder.createAccessRequestPacket(username,
									clientInfo.getEnvironmentMap()
											.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.PASSWORD),
									config.getRadiusSharedSecret(), config.getOvpnNasIpAddress(),
									config.getOvpnNasPort(), config.getPacketId().getAndIncrement());
							RadPacket accessResponse = RadClient.sendReceiveRadiusPacket(accessRequest, config);
							if (accessResponse != null
									&& accessResponse.getPacketType() == RadPacket.RadPacketType.ACCESS_ACCEPT) {
								Optional<RadAttribute> framedIpAddress = accessResponse.getAttributes().stream()
										.filter(e -> e.getAttributeType() == RadAttributeType.FRAMED_IP_ADDRESS)
										.findFirst();
								if (framedIpAddress.isPresent()) {
									IPv4Address framedIpAddressConverted = new IPv4Address(
											framedIpAddress.get().getAttributeValue());
									ClientSession session = new ClientSession();
									session.setUsername(username);
									session.setSessionStartTime(LocalDateTime.now(ZoneOffset.UTC));
									String clientPublicIp = clientInfo.getEnvironmentMap()
											.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.UNTRUSTED_IP);
									String clientPublicPort = clientInfo.getEnvironmentMap()
											.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.UNTRUSTED_PORT);
									session.setCallingStationId(clientPublicIp + ":" + clientPublicPort);
									session.setRadiusSessionId(UUID.randomUUID().toString());
									session.setFramedIpAddress(framedIpAddressConverted);
									session.setOvpnInternalSessionId(controlFile.getName());
									sessionStore.addSession(session);
									sessionStore.writeSessionsToFile(config);
									sessionStore.writeClientConfigurationFile(accessResponse, clientInfo, config);
									RadPacket acctStart = RadPacket.RadPacketBuilder.createAccountingRequestPacket(
											session.getUsername(), config.getRadiusSharedSecret(),
											config.getOvpnNasIpAddress(), config.getOvpnNasPort(),
											RadAttribute.RadAccountingStatusType.START, session.getFramedIpAddress(),
											session.getCallingStationId(), 0, 0, 0, session.getRadiusSessionId(),
											RadAttribute.RadAccountingAuthenticationMode.RADIUS, 0, 0, 0,
											RadAttribute.RadAccountingTerminationCause.IGNORE,
											config.getPacketId().getAndIncrement());
									RadClient.sendReceiveRadiusPacket(acctStart, config);
								}
								acceptClient = true;
							}
						} catch (SharedSecretMissingException ignored) {
							config.getLogger().error("Shared secret is missing from the message.");
						} finally {
							config.getLogger().debug(String.format("Writing %d to file %s", (acceptClient ? 1 : 0),
									controlFile.getAbsolutePath()));
							controlFileWriter.println(acceptClient ? "1" : "0");
							controlFileWriter.close();
						}
					} else if (acceptedMessage.startsWith("STOP#")) {
						String[] parameters = acceptedMessage.split("#");

						ClientEnvironmentInfo clientInfo = ClientEnvironmentInfo.parseEnvironmentData(parameters);
						try {
							String username = clientInfo.getEnvironmentMap()
									.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.USERNAME);
							File controlFile = new File(clientInfo.getEnvironmentMap()
									.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.CONTROL_FILE));
							long bytesSent = Long.parseLong(clientInfo.getEnvironmentMap()
									.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.BYTES_SENT));
							long bytesReceived = Long.parseLong(clientInfo.getEnvironmentMap()
									.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.BYTES_RECEIVED));
							String ovpnInternalSessionId = controlFile.getName();
							Optional<ClientSession> optionalSession = sessionStore.getSession(username,
									ovpnInternalSessionId);
							if (optionalSession.isPresent()) {
								ClientSession session = optionalSession.get();
								RadPacket acctStop = RadPacket.RadPacketBuilder.createAccountingRequestPacket(
										session.getUsername(), config.getRadiusSharedSecret(),
										config.getOvpnNasIpAddress(), config.getOvpnNasPort(),
										RadAttribute.RadAccountingStatusType.STOP, session.getFramedIpAddress(),
										session.getCallingStationId(), 0, bytesSent, bytesReceived,
										session.getRadiusSessionId(),
										RadAttribute.RadAccountingAuthenticationMode.RADIUS,
										(int) session.getSessionStartTime().until(LocalDateTime.now(),
												ChronoUnit.SECONDS),
										0, 0, RadAttribute.RadAccountingTerminationCause.USER_REQUEST,
										config.getPacketId().getAndIncrement());
								RadClient.sendReceiveRadiusPacket(acctStop, config);
								sessionStore.removeSession(session);
								sessionStore.writeSessionsToFile(config);
							}
						} catch (SharedSecretMissingException ignored) {
							config.getLogger().error("Shared secret is missing from the message.");
						}
					} else {
						writer.write(buffer, 0, messageLength);
						writer.flush();
					}
				} else {
					config.getLogger().debug("Received a message with an invalid signature.");
				}
				reader.close();
				writer.close();
				clientSocket.close();
			}
		} catch (IOException ex) {
			config.getLogger().error(ex.getMessage(), ex);
		}
	}

	private String validateSignatureAndExtractMessage(String fullMessage) {
		try {
			String[] messageElements = fullMessage.split("::");
			byte[] originalMessageBytes = messageElements[0].getBytes(StandardCharsets.UTF_8);
			byte[] internalSharedSecretBytes = config.getInternalPluginSharedSecret().getBytes(StandardCharsets.UTF_8);
			byte[] hashSource = new byte[originalMessageBytes.length + internalSharedSecretBytes.length];
			System.arraycopy(originalMessageBytes, 0, hashSource, 0, originalMessageBytes.length);
			System.arraycopy(internalSharedSecretBytes, 0, hashSource, originalMessageBytes.length,
					internalSharedSecretBytes.length);
			byte[] signature = PasswordUtil.calculateMd5(hashSource);
			if (Arrays.equals(signature, Base64.getDecoder().decode(messageElements[1]))) {
				return messageElements[0].trim();
			} else {
				return null;
			}
		} catch (NullPointerException ex) {
			return null;
		}
	}
}
