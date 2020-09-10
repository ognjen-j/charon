package ognjenj.charon.acct.integration;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ognjenj.charon.acct.exceptions.GenericAccountingException;
import ognjenj.charon.acct.exceptions.OvpnClientStatusFileNotFoundException;
import ognjenj.charon.acct.ovpn.OvpnClientStatus;
import ognjenj.charon.acct.ovpn.OvpnClientStatusFileParser;
import ognjenj.charon.acct.runner.PluginConfiguration;

public class EnvironmentTest {
	PluginConfiguration config;

	@Before
	public void setupUpConfig() {
		config = new PluginConfiguration();
		config.setOvpnClientStatusFile("src/test/resources/teststatus.log");
	}

	@Test
	public void testReadingStatusFile()
			throws IOException, OvpnClientStatusFileNotFoundException, GenericAccountingException {
		Map<String, OvpnClientStatus> clientStatuses = OvpnClientStatusFileParser
				.parseClientStatusFile(config.getOvpnClientStatusFile());
		Assert.assertEquals(clientStatuses.size(), 2);
		Assert.assertEquals(clientStatuses.get("test2").getSessionStartTime().getYear(), 1980);
		Assert.assertEquals(clientStatuses.get("test2").getSessionStartTime().getHour(), 9);
		Assert.assertEquals(clientStatuses.get("test").getSessionStartTime().getHour(), 16);
	}
}
