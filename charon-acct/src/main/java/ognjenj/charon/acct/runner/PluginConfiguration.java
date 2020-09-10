package ognjenj.charon.acct.runner;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;

public class PluginConfiguration implements Serializable {
	private static final String OVPN_NAS_PORT = "ovpn_nas_port";
	private static final String RADIUS_ACCOUNTING_PORT = "radius_accounting_port";
	private static final String RADIUS_AUTHENTICATION_PORT = "radius_authentication_port";
	private static final String RADIUS_SHARED_SECRET = "radius_shared_secret";
	private static final String RADIUS_NETWORK_TIMEOUT_LENGTH = "radius_network_timeout_length";
	private static final String OVPN_CLIENT_STATUS_FILE = "ovpn_client_status_file";
	private static final String OVPN_CCD_DIRECTORY = "ovpn_ccd_directory";
	private static final String PLUGIN_INTERNAL_TCP_PORT = "plugin_internal_tcp_port";
	private static final String PLUGIN_INTERNAL_SHARED_SECRET = "plugin_internal_shared_secret";
	private static final String PLUGIN_SESSIONS_DIRECTORY = "plugin_sessions_directory";
	private final Logger logger = LogManager.getLogger(Runner.class);
	private IPv4Address radiusIpAddress;
	private int ovpnNasPort = 5;
	private IPv4Address ovpnNasIpAddress = (IPv4Address) (new IPAddressString("127.0.0.1")
			.getAddress(IPAddress.IPVersion.IPV4));
	private int radiusAuthPort = 1812;
	private int radiusAcctPort = 1813;
	private String radiusSharedSecret;
	private int networkTimeoutLength = 30;
	private String ovpnClientStatusFile;
	private String ovpnCcdDirectory = "/etc/openvpn/ccd";
	private int internalPluginServerTcpPort = 9000;
	private String internalPluginSharedSecret = "testing123";
	private String internalPluginSessionsDirectory = "/etc/openvpn/sessions";
	private AtomicInteger packetId = new AtomicInteger(1);

	public static PluginConfiguration parseConfigurationFile(String configurationFileLocation)
			throws IOException, AddressStringException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(configurationFileLocation)));
		PluginConfiguration configuration = new PluginConfiguration();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] elements = line.split("=");
			if (elements.length == 2) {
				String optionName = elements[0].trim();
				String optionValue = elements[1].trim();
				switch (optionName) {
					case OVPN_NAS_PORT :
						configuration.setOvpnNasPort(Integer.parseInt(optionValue));
						break;
					case RADIUS_ACCOUNTING_PORT :
						configuration.setRadiusAcctPort(Integer.parseInt(optionValue));
						break;
					case RADIUS_AUTHENTICATION_PORT :
						configuration.setRadiusAuthPort(Integer.parseInt(optionValue));
						break;
					case RADIUS_SHARED_SECRET :
						configuration.setRadiusSharedSecret(optionValue);
						break;
					case RADIUS_NETWORK_TIMEOUT_LENGTH :
						configuration.setNetworkTimeoutLength(Integer.parseInt(optionValue));
						break;
					case OVPN_CLIENT_STATUS_FILE :
						configuration.setOvpnClientStatusFile(optionValue);
						break;
					case OVPN_CCD_DIRECTORY :
						configuration.setOvpnCcdDirectory(optionValue);
						break;
					case PLUGIN_INTERNAL_TCP_PORT :
						configuration.setInternalPluginServerTcpPort(Integer.parseInt(optionValue));
						break;
					case PLUGIN_INTERNAL_SHARED_SECRET :
						configuration.setInternalPluginSharedSecret(optionValue);
						break;
					case PLUGIN_SESSIONS_DIRECTORY :
						configuration.setInternalPluginSessionsDirectory(optionValue);
						break;
				}
			}
		}
		configuration.setOvpnNasIpAddress(
				(IPv4Address) (new IPAddressString("10.41.125.2").toAddress(IPAddress.IPVersion.IPV4)));
		configuration.setRadiusIpAddress(
				(IPv4Address) (new IPAddressString("10.41.125.3").toAddress(IPAddress.IPVersion.IPV4)));
		configuration.getPacketId().set(1);
		return configuration;
	}

	public AtomicInteger getPacketId() {
		return packetId;
	}

	public String getRadiusSharedSecret() {
		return radiusSharedSecret;
	}

	public void setRadiusSharedSecret(String radiusSharedSecret) {
		this.radiusSharedSecret = radiusSharedSecret;
	}

	public String getOvpnCcdDirectory() {
		return ovpnCcdDirectory;
	}

	public void setOvpnCcdDirectory(String ovpnCcdDirectory) {
		this.ovpnCcdDirectory = ovpnCcdDirectory;
	}

	public String getInternalPluginSessionsDirectory() {
		return internalPluginSessionsDirectory;
	}

	public void setInternalPluginSessionsDirectory(String internalPluginSessionsDirectory) {
		this.internalPluginSessionsDirectory = internalPluginSessionsDirectory;
	}

	public String getInternalPluginSharedSecret() {
		return internalPluginSharedSecret;
	}

	public void setInternalPluginSharedSecret(String internalPluginSharedSecret) {
		this.internalPluginSharedSecret = internalPluginSharedSecret;
	}

	public Logger getLogger() {
		return logger;
	}

	public int getInternalPluginServerTcpPort() {
		return internalPluginServerTcpPort;
	}

	public void setInternalPluginServerTcpPort(int internalPluginServerTcpPort) {
		this.internalPluginServerTcpPort = internalPluginServerTcpPort;
	}

	public String getOvpnClientStatusFile() {
		return ovpnClientStatusFile;
	}

	public void setOvpnClientStatusFile(String ovpnClientStatusFile) {
		this.ovpnClientStatusFile = ovpnClientStatusFile;
	}

	public IPv4Address getOvpnNasIpAddress() {
		return ovpnNasIpAddress;
	}

	public void setOvpnNasIpAddress(IPv4Address ovpnNasIpAddress) {
		this.ovpnNasIpAddress = ovpnNasIpAddress;
	}

	public int getOvpnNasPort() {
		return ovpnNasPort;
	}

	public void setOvpnNasPort(int ovpnNasPort) {
		this.ovpnNasPort = ovpnNasPort;
	}

	public int getNetworkTimeoutLength() {
		return networkTimeoutLength;
	}

	public void setNetworkTimeoutLength(int networkTimeoutLength) {
		this.networkTimeoutLength = networkTimeoutLength;
	}

	public IPv4Address getRadiusIpAddress() {
		return radiusIpAddress;
	}

	public void setRadiusIpAddress(IPv4Address radiusIpAddress) {
		this.radiusIpAddress = radiusIpAddress;
	}

	public int getRadiusAuthPort() {
		return radiusAuthPort;
	}

	public void setRadiusAuthPort(int radiusAuthPort) {
		this.radiusAuthPort = radiusAuthPort;
	}

	public int getRadiusAcctPort() {
		return radiusAcctPort;
	}

	public void setRadiusAcctPort(int radiusAcctPort) {
		this.radiusAcctPort = radiusAcctPort;
	}
}
