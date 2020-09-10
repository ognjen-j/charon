package ognjenj.charon.acct.runner;

import java.io.IOException;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class InternalPluginServer {
	private final PluginConfiguration config;
	private final AtomicBoolean acceptConnections = new AtomicBoolean();
	private final AtomicBoolean runAccountingThread;

	public InternalPluginServer(PluginConfiguration config, AtomicBoolean runAccountingThread) {
		this.config = config;
		this.acceptConnections.set(true);
		this.runAccountingThread = runAccountingThread;
	}

	public void startInternalServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(config.getInternalPluginServerTcpPort(), 100,
					Inet4Address.getLoopbackAddress());
			while (acceptConnections.get()) {
				Socket clientSocket = serverSocket.accept();
				new InternalPluginServerThread(clientSocket, config, acceptConnections, runAccountingThread);
			}
		} catch (BindException ex) {
			config.getLogger().error("Address is already in use, probably another instance. Exiting.");
			System.exit(1);
		} catch (IOException ex) {
			config.getLogger().error(ex.getMessage(), ex);
		}
	}
}
