package ognjenj.charon.acct.ovpn;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import ognjenj.charon.acct.radius.RadAttribute;
import ognjenj.charon.acct.radius.RadAttributeType;
import ognjenj.charon.acct.radius.RadAttributeVendor;
import ognjenj.charon.acct.radius.RadPacket;
import ognjenj.charon.acct.runner.PluginConfiguration;
import ognjenj.charon.acct.util.StringIpHelper;

public class ClientSessionStore {
	private static final ClientSessionStore INSTANCE;

	static {
		INSTANCE = new ClientSessionStore();
	}

	private Set<ClientSession> sessions = new HashSet<>();

	private ClientSessionStore() {
	}

	public static ClientSessionStore getInstance() {
		return INSTANCE;
	}

	public synchronized void addSession(ClientSession session) {
		this.sessions.add(session);
	}

	public synchronized void removeSession(ClientSession session) {
		this.sessions.remove(session);
	}

	public Optional<ClientSession> getSession(String username, String ovpnInternalSessionId) {
		return sessions.stream().filter(
				e -> e.getUsername().equals(username) && e.getOvpnInternalSessionId().equals(ovpnInternalSessionId))
				.findFirst();
	}

	public synchronized Optional<ClientSession> getNewestSessionForUsername(String username) {
		return sessions.stream().filter(e -> e.getUsername().equals(username))
				.min(Comparator.comparing(ClientSession::getSessionStartTime));
	}

	public synchronized void cleanAllSessionFiles(PluginConfiguration config) {
		File sessionsDirectory = new File(config.getInternalPluginSessionsDirectory());
		if (!sessionsDirectory.exists()) {
			sessionsDirectory.mkdirs();
		}
		for (File sessionsFile : sessionsDirectory.listFiles()) {
			try {
				Files.delete(sessionsFile.toPath());
			} catch (IOException ex) {
				config.getLogger().error(ex.getMessage(), ex);
			}
		}
	}

	public synchronized void writeSessionsToFile(PluginConfiguration config) throws IOException {
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(new File(config.getInternalPluginSessionsDirectory(), "sessions"), false)),
				true);
		for (ClientSession session : sessions) {
			writer.println(String.format("%s:%s:%s:%s:%s:%d", session.getUsername(), session.getOvpnInternalSessionId(),
					session.getRadiusSessionId(), session.getCallingStationId(),
					session.getFramedIpAddress().toCanonicalString(),
					session.getSessionStartTime().toEpochSecond(ZoneOffset.UTC)));
		}
		writer.close();
	}

	public synchronized void readSessionsFromFile(PluginConfiguration config) throws IOException {
		File sessionsFile = new File(config.getInternalPluginSessionsDirectory(), "sessions");
		if (sessionsFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(sessionsFile));
			sessions = new HashSet<>();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] elements = line.trim().split(":");
				ClientSession session = new ClientSession();
				session.setUsername(elements[0]);
				session.setOvpnInternalSessionId(elements[1]);
				session.setRadiusSessionId(elements[2]);
				session.setCallingStationId(String.format("%s:%s", elements[3], elements[4]));
				session.setFramedIpAddress(
						(IPv4Address) (new IPAddressString(elements[5]).getAddress(IPAddress.IPVersion.IPV4)));
				session.setSessionStartTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(elements[6])),
						ZoneOffset.UTC.normalized()));
				sessions.add(session);
			}
			reader.close();
		}
	}

	public synchronized void writeClientConfigurationFile(RadPacket accessAcceptPacket,
			ClientEnvironmentInfo clientInfo, PluginConfiguration config) throws IOException {
		PrintWriter clientConfigWriter = new PrintWriter(
				new BufferedWriter(
						new FileWriter(
								new File(config.getOvpnCcdDirectory(),
										clientInfo.getEnvironmentMap()
												.get(ClientEnvironmentInfo.ClientEnvironmentAttribute.USERNAME)),
								false)),
				true);
		Optional<RadAttribute> framedIpAddress = accessAcceptPacket.getAttributes().stream()
				.filter(e -> e.getAttributeType() == RadAttributeType.FRAMED_IP_ADDRESS).findFirst();
		Optional<RadAttribute> framedIpNetmask = accessAcceptPacket.getAttributes().stream()
				.filter(e -> e.getAttributeType() == RadAttributeType.FRAMED_IP_NETMASK).findFirst();
		List<RadAttribute> framedRoutes = accessAcceptPacket.getAttributes().stream()
				.filter(e -> e.getAttributeType() == RadAttributeType.FRAMED_ROUTE).collect(Collectors.toList());
		List<RadAttribute> dnsServers = accessAcceptPacket.getAttributes().stream()
				.filter(e -> e.getAttributeType() == RadAttributeType.VENDOR_SPECIFIC
						&& e.getVendorSpecificInfo() != null
						&& e.getVendorSpecificInfo().getVendor() == RadAttributeVendor.RAD_VENDOR_MICROSOFT
						&& Arrays.asList(RadAttributeVendor.RadVendorSpecificAttribute.RAD_VENDOR_MICROSOFT_PRIMARY_DNS,
								RadAttributeVendor.RadVendorSpecificAttribute.RAD_VENDOR_MICROSOFT_SECONDARY_DNS)
								.contains(e.getVendorSpecificInfo().getAttributeType()))
				.collect(Collectors.toList());
		if (framedIpAddress.isPresent() && framedIpNetmask.isPresent()) {
			IPAddress framedIpAddressConverted = new IPv4Address(framedIpAddress.get().getAttributeValue());
			IPAddress framedIpNetmaskConverted = new IPv4Address(framedIpNetmask.get().getAttributeValue());

			clientConfigWriter.println(String.format("ifconfig-push %s %s",
					framedIpAddressConverted.toConvertedString(), framedIpNetmaskConverted.toConvertedString()));
			clientConfigWriter.println("push \"compress lzo\"");
			List<String> convertedRoutes = new ArrayList<>();
			for (RadAttribute routeAttribute : framedRoutes) {
				convertedRoutes.add(StringIpHelper.transformRouteNotation(
						new String(routeAttribute.getAttributeValue(), StandardCharsets.US_ASCII)));
			}
			if (convertedRoutes.contains(StringIpHelper.DEFAULT_ROUTE)) {
				clientConfigWriter.println("push \"redirect-gateway def1\"");
			} else {
				for (String convertedRoute : convertedRoutes) {
					clientConfigWriter.println(String.format("push \"route %s\"", convertedRoute));
				}
			}
			for (RadAttribute dnsServer : dnsServers) {
				byte[] dnsServerAttributeValue = dnsServer.getVendorSpecificAttributeValue();
				IPAddress dnsServerConverted = new IPv4Address(dnsServerAttributeValue);
				clientConfigWriter
						.println(String.format("push \"dhcp-option DNS %s\"", dnsServerConverted.toConvertedString()));
			}
		}
		clientConfigWriter.close();
	}
}
