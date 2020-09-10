package ognjenj.charon.acct.runner;

import java.util.concurrent.atomic.AtomicBoolean;

import ognjenj.charon.acct.ovpn.ClientSessionStore;

public class Runner {
	public static void main(String[] args) {
		try {
			PluginConfiguration pluginConfiguration = PluginConfiguration.parseConfigurationFile(args[0]);
			AtomicBoolean runAccountingThread = new AtomicBoolean();
			runAccountingThread.set(true);
			if (args[1].equals("1")) {
				ClientSessionStore.getInstance().cleanAllSessionFiles(pluginConfiguration);
			}
			new AcctUpdateThread(pluginConfiguration, runAccountingThread);
			InternalPluginServer tcpServer = new InternalPluginServer(pluginConfiguration, runAccountingThread);
			tcpServer.startInternalServer();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
