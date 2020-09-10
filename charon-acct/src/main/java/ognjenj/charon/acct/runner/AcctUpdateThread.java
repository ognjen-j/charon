package ognjenj.charon.acct.runner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import ognjenj.charon.acct.exceptions.GenericAccountingException;
import ognjenj.charon.acct.exceptions.OvpnClientStatusFileNotFoundException;
import ognjenj.charon.acct.exceptions.SharedSecretMissingException;
import ognjenj.charon.acct.network.RadClient;
import ognjenj.charon.acct.ovpn.ClientSession;
import ognjenj.charon.acct.ovpn.ClientSessionStore;
import ognjenj.charon.acct.ovpn.OvpnClientStatus;
import ognjenj.charon.acct.ovpn.OvpnClientStatusFileParser;
import ognjenj.charon.acct.radius.RadAttribute;
import ognjenj.charon.acct.radius.RadPacket;

public class AcctUpdateThread extends Thread {

	private final PluginConfiguration config;
	private final AtomicBoolean runThread;

	public AcctUpdateThread(PluginConfiguration config, AtomicBoolean runThread) {
		this.config = config;
		this.runThread = runThread;
		this.runThread.set(true);
		start();
	}

	public synchronized void run() {
		while (runThread.get()) {
			try {
				Thread.sleep(30000);
				ClientSessionStore sessionStore = ClientSessionStore.getInstance();
				sessionStore.readSessionsFromFile(config);
				Map<String, OvpnClientStatus> clientStatuses = OvpnClientStatusFileParser
						.parseClientStatusFile(config.getOvpnClientStatusFile());
				for (Map.Entry<String, OvpnClientStatus> clientStatusEntry : clientStatuses.entrySet()) {
					Optional<ClientSession> optionalSession = sessionStore
							.getNewestSessionForUsername(clientStatusEntry.getKey());
					if (optionalSession.isPresent()) {
						ClientSession session = optionalSession.get();
						RadPacket acctInterim = RadPacket.RadPacketBuilder.createAccountingRequestPacket(
								session.getUsername(), config.getRadiusSharedSecret(), config.getOvpnNasIpAddress(),
								config.getOvpnNasPort(), RadAttribute.RadAccountingStatusType.INTERIM_ACCT,
								session.getFramedIpAddress(), session.getCallingStationId(), 0,
								clientStatusEntry.getValue().getBytesSent(),
								clientStatusEntry.getValue().getBytesReceived(), session.getRadiusSessionId(),
								RadAttribute.RadAccountingAuthenticationMode.RADIUS,
								(int) session.getSessionStartTime().until(LocalDateTime.now(), ChronoUnit.SECONDS), 0,
								0, RadAttribute.RadAccountingTerminationCause.IGNORE,
								(int) config.getPacketId().getAndIncrement());
						RadClient.sendReceiveRadiusPacket(acctInterim, config);
					}
				}
			} catch (InterruptedException | IOException | OvpnClientStatusFileNotFoundException
					| SharedSecretMissingException | GenericAccountingException ex) {
				config.getLogger().error(ex.getMessage(), ex);
			}
		}
	}
}
