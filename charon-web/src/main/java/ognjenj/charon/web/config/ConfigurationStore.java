package ognjenj.charon.web.config;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;

import ognjenj.charon.web.exceptions.IncompleteConfigurationException;

public class ConfigurationStore {
	private static final ConfigurationStore INSTANCE;

	static {
		String dbHost = System.getenv("RADIUS_DB_ADDRESS");
		String dbUsername = System.getenv("RADIUS_DB_USERNAME");
		String dbPassword = System.getenv("RADIUS_DB_PASSWORD");
		String dbTimezone = System.getenv("RADIUS_DB_TIMEZONE");
		String envWebServerPort = System.getenv("WEB_SERVER_PORT");
		String caCertificate = System.getenv("CA_CERTIFICATE");
		String caKey = System.getenv("CA_KEY");
		String caKeyPass = System.getenv("CA_KEY_PASS");
		String caHome = System.getenv("CA_HOME");
		String crlHome = System.getenv("CRL_HOME");
		String caDownloadsHome = System.getenv("CA_DOWNLOADS_HOME");
		String webServerAddress = System.getenv("WEB_SERVER_ADDRESS");
		String ovpnPublicAddress = System.getenv("OVPN_PUBLIC_IP_ADDRESS");
		int ovpnPublicPort = Integer.parseInt(System.getenv("OVPN_PUBLIC_PORT"));
		String ovpnCipher = System.getenv("OVPN_CIPHER");
		String ovpnDigest = System.getenv("OVPN_DIGEST");
		String webServerKeystore = System.getenv("WEB_SERVER_KEYSTORE");
		String webServerKeystorePass = System.getenv("WEB_SERVER_KEYSTORE_PASS");
		int keyLength = Integer.parseInt(System.getenv("KEY_LENGTH"));
		int webServerPort = 80;
		try {
			if (dbHost == null) {
				throw new IncompleteConfigurationException(
						"Please provide the address of the database through the RADIUS_DB_ADDRESS environment variable");
			}
			if (dbUsername == null) {
				throw new IncompleteConfigurationException(
						"Please provide the username for the database connection through the RADIUS_DB_USERNAME environment variable");
			}
			if (dbPassword == null) {
				throw new IncompleteConfigurationException(
						"Please provide the password for the database connection through the RADIUS_DB_PASSWORD environment variable");
			}
			if (dbTimezone == null) {
				dbTimezone = "Europe/Berlin";
			}
			if (caCertificate == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the CA certificate through the CA_CERTIFICATE environment variable");
			}
			if (caKey == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the CA key through the CA_KEY environment variable");
			}
			if (caKeyPass == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the CA key password through the CA_KEY_PASS environment variable");
			}
			if (caHome == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the CA home directory through the CA_HOME environment variable");
			}
			if (crlHome == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the CRL home directory through the CRL_HOME environment variable");
			}
			if (webServerAddress == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the public server IP address through the WEB_SERVER_ADDRESS environment variable");
			}
			if (webServerKeystore == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the PKCS12 keystore through the WEB_SERVER_KEYSTORE environment variable");
			}
			if (webServerKeystorePass == null) {
				throw new IncompleteConfigurationException(
						"Please provide the keystore password through the WEB_SERVER_KEYSTORE_PASS environment variable");
			}
			if (ovpnPublicAddress == null) {
				throw new IncompleteConfigurationException(
						"Please provide the location of the public OVPN IP address through the OVPN_PUBLIC_IP_ADDRESS environment variable");
			}
			if (ovpnCipher == null) {
				throw new IncompleteConfigurationException(
						"Please provide the OVPN cipher algorithm through the OVPN_CIPHER environment variable");
			}
			if (ovpnDigest == null) {
				throw new IncompleteConfigurationException(
						"Please provide the OVPN digest algorithm through the OVPN_DIGEST environment variable");
			}

			if (envWebServerPort != null && !envWebServerPort.isBlank()) {
				try {
					webServerPort = Integer.parseInt(envWebServerPort);
				} catch (NumberFormatException ignored) {
				}
			}
			File certSerialIndexFile = new File(caHome, "serial");
			if (!certSerialIndexFile.exists()) {
				initializeCertSerial(certSerialIndexFile);
			}
			File crlSerialIndexFile = new File(caHome, "crlserial");
			if (!crlSerialIndexFile.exists()) {
				initializeCrlSerial(crlSerialIndexFile);
			}
		} catch (IncompleteConfigurationException | IOException ex) {
			ex.printStackTrace(System.err);
			System.exit(-1);
		}
		INSTANCE = new ConfigurationStore();
		INSTANCE.dbTimezone = dbTimezone;
		INSTANCE.radiusDbHost = dbHost;
		INSTANCE.radiusDbUsername = dbUsername;
		INSTANCE.radiusDbPassword = dbPassword;
		INSTANCE.webServerPort = webServerPort;
		INSTANCE.webServerAddress = webServerAddress;
		INSTANCE.webServerKeystore = webServerKeystore;
		INSTANCE.webServerKeystorePass = webServerKeystorePass;
		INSTANCE.caCertificate = caCertificate;
		INSTANCE.caKey = caKey;
		INSTANCE.caKeyPass = caKeyPass;
		INSTANCE.caHome = caHome;
		INSTANCE.crlHome = crlHome;
		INSTANCE.caDownloadsHome = caDownloadsHome;
		INSTANCE.keyLength = keyLength;
		INSTANCE.ovpnCipher = ovpnCipher;
		INSTANCE.ovpnDigest = ovpnDigest;
		INSTANCE.ovpnPublicAddress = ovpnPublicAddress;
		INSTANCE.ovpnPublicPort = ovpnPublicPort;
	}

	private int webServerPort;
	private String webServerAddress;
	private String webServerKeystore;
	private String webServerKeystorePass;
	private String dbTimezone;
	private String radiusDbUsername;
	private String radiusDbPassword;
	private String radiusDbHost;
	private String caCertificate;
	private String caKey;
	private String caKeyPass;
	private String caHome;
	private String crlHome;
	private String caDownloadsHome;
	private String ovpnPublicAddress;
	private int ovpnPublicPort;
	private String ovpnCipher;
	private String ovpnDigest;
	private int keyLength;
	private ConfigurationStore() {
	}

	private static synchronized void initializeCertSerial(File serialIndexFile) throws IOException {
		PrintWriter serialWriter = new PrintWriter(new BufferedWriter(new FileWriter(serialIndexFile, false)), true);
		BigInteger newSerial = BigInteger.probablePrime(128, new Random());
		serialWriter.println(newSerial.toString(16));
		serialWriter.close();
	}

	private static synchronized void initializeCrlSerial(File serialIndexFile) throws IOException {
		PrintWriter serialWriter = new PrintWriter(new BufferedWriter(new FileWriter(serialIndexFile, false)), true);
		BigInteger newSerial = BigInteger.probablePrime(128, new Random());
		serialWriter.println(newSerial.toString(16));
		serialWriter.close();
	}

	public static ConfigurationStore getInstance() {
		return INSTANCE;
	}

	public String getCaKeyPass() {
		return caKeyPass;
	}

	public String getCaKey() {
		return caKey;
	}

	public void setCaKey(String caKey) {
		this.caKey = caKey;
	}

	public String getCaCertificate() {
		return caCertificate;
	}

	public String getWebServerKeystore() {
		return webServerKeystore;
	}

	public void setWebServerKeystore(String webServerKeystore) {
		this.webServerKeystore = webServerKeystore;
	}

	public String getWebServerKeystorePass() {
		return webServerKeystorePass;
	}

	public void setWebServerKeystorePass(String webServerKeystorePass) {
		this.webServerKeystorePass = webServerKeystorePass;
	}

	public String getOvpnPublicAddress() {
		return ovpnPublicAddress;
	}

	public void setOvpnPublicAddress(String ovpnPublicAddress) {
		this.ovpnPublicAddress = ovpnPublicAddress;
	}

	public int getOvpnPublicPort() {
		return ovpnPublicPort;
	}

	public String getOvpnCipher() {
		return ovpnCipher;
	}

	public String getOvpnDigest() {
		return ovpnDigest;
	}

	public String getCrlHome() {
		return crlHome;
	}

	public void setCrlHome(String crlHome) {
		this.crlHome = crlHome;
	}

	public synchronized BigInteger getNextCertSerial() throws IOException {
		File serialIndexFile = new File(caHome, "serial");
		if (!serialIndexFile.exists()) {
			ConfigurationStore.initializeCertSerial(serialIndexFile);
		}
		BufferedReader serialReader = new BufferedReader(new FileReader(serialIndexFile));
		String hexString = serialReader.readLine();
		serialReader.close();
		BigInteger previousSerial = new BigInteger(hexString, 16);
		BigInteger newSerial = previousSerial.add(BigInteger.ONE);
		try (PrintWriter serialWriter = new PrintWriter(new BufferedWriter(new FileWriter(serialIndexFile, false)),
				true)) {
			serialWriter.println(newSerial.toString(16));
		}
		return newSerial;
	}

	public synchronized BigInteger getNextCrlSerial() throws IOException {
		File serialIndexFile = new File(caHome, "crlserial");
		if (!serialIndexFile.exists()) {
			ConfigurationStore.initializeCrlSerial(serialIndexFile);
		}
		BufferedReader serialReader = new BufferedReader(new FileReader(serialIndexFile));
		String hexString = serialReader.readLine();
		serialReader.close();
		BigInteger previousSerial = new BigInteger(hexString, 16);
		BigInteger newSerial = previousSerial.add(BigInteger.ONE);
		try (PrintWriter serialWriter = new PrintWriter(new BufferedWriter(new FileWriter(serialIndexFile, false)),
				true)) {
			serialWriter.println(newSerial.toString(16));
		}
		return newSerial;
	}

	public String getWebServerAddress() {
		return webServerAddress;
	}

	public void setWebServerAddress(String webServerAddress) {
		this.webServerAddress = webServerAddress;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public String getCaHome() {
		return caHome;
	}

	public void setCaHome(String caHome) {
		this.caHome = caHome;
	}

	public String getCaDownloadsHome() {
		return caDownloadsHome;
	}

	public void setCaDownloadsHome(String caDownloadsHome) {
		this.caDownloadsHome = caDownloadsHome;
	}

	public int getWebServerPort() {
		return webServerPort;
	}

	public String getDbTimezone() {
		return dbTimezone;
	}

	public void setDbTimezone(String dbTimezone) {
		this.dbTimezone = dbTimezone;
	}

	public String getRadiusDbUsername() {
		return radiusDbUsername;
	}

	public void setRadiusDbUsername(String radiusDbUsername) {
		this.radiusDbUsername = radiusDbUsername;
	}

	public String getRadiusDbPassword() {
		return radiusDbPassword;
	}

	public void setRadiusDbPassword(String radiusDbPassword) {
		this.radiusDbPassword = radiusDbPassword;
	}

	public String getRadiusDbHost() {
		return radiusDbHost;
	}

}
