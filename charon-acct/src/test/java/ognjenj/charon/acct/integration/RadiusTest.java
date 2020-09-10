package ognjenj.charon.acct.integration;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import ognjenj.charon.acct.exceptions.ResponseValidationException;
import ognjenj.charon.acct.exceptions.SharedSecretMissingException;
import ognjenj.charon.acct.network.RadClient;
import ognjenj.charon.acct.ovpn.ClientEnvironmentInfo;
import ognjenj.charon.acct.ovpn.ClientSessionStore;
import ognjenj.charon.acct.radius.RadAttribute;
import ognjenj.charon.acct.radius.RadPacket;
import ognjenj.charon.acct.runner.PluginConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RadiusTest {

  PluginConfiguration config;
  PluginConfiguration configNoServer;
  ClientEnvironmentInfo clientInfo;
  RadPacket accessRequest;
  String username = "test";
  //String password = "test";
  String password = "$6$TGMCMCXG$HwKjfq.Jif8yoz8W9Kpr5.JYm5RXVNEF0opS/a7RPGKt1KYxh/LzO0fKNlynmBxfkznAJrtlDaP1vztXuwDjm1";
  String sharedSecret = "testing123";

  String[] clientEnvironmentData =
      new String[] {
        "auth_control_file=/etc/openvpn/tmp/openvpn_acf_6762af75bd21221f2cba5efa15d8e6cd.tmp",
        "untrusted_port=58716",
        "untrusted_ip=192.168.0.100",
        "common_name=Client1",
        "password=$6$TGMCMCXG$HwKjfq.Jif8yoz8W9Kpr5.JYm5RXVNEF0opS/a7RPGKt1KYxh/LzO0fKNlynmBxfkznAJrtlDaP1vztXuwDjm1",
        "username=someuser",
        "IV_TCPNL=1",
        "IV_COMP_STUBv2=1",
        "IV_COMP_STUB=1",
        "IV_LZO=1",
        "IV_LZ4v2=1",
        "IV_LZ4=1",
        "IV_NCP=2",
        "IV_PROTO=2",
        "IV_PLAT=linux",
        "IV_VER=2.4.4",
        "tls_serial_hex_0=01",
        "tls_serial_0=1",
        "tls_digest_sha256_0=81:26:5d:70:66:62:3d:fd:e1:e8:04:52:5a:56:41:80:b0:6c:7a:f6:31:3e:6b:45:12:f2:5f:64:00:bd:ce:e6",
        "tls_digest_0=72:c9:9b:1d:b3:2c:90:8f:7b:9b:6b:21:bd:f9:19:84:22:f2:c4:fe",
        "tls_id_0=C=DE, ST=Nordrhein-Westfalen, L=Koeln, O=VPN Company GmbH, OU=IT department, CN=Client1, emailAddress=client1@email.com",
        "X509_0_emailAddress=client1@email.com",
        "X509_0_CN=Client1",
        "X509_0_OU=IT department",
        "X509_0_O=VPN Company GmbH",
        "X509_0_L=Koeln",
        "X509_0_ST=Nordrhein-Westfalen",
        "X509_0_C=DE",
        "tls_serial_hex_1=07:64:a7:89:65:8b:4f:54:cc:9b:fd:af:ea:55:73:07:2f:59:ce:7a",
        "tls_serial_1=42207604421030176980005767038587512158693215866",
        "tls_digest_sha256_1=c3:f7:77:bb:58:db:c7:a0:7f:6c:72:a8:56:85:20:b6:62:8d:4c:d0:42:1d:67:13:05:2b:71:b0:77:e6:40:7d",
        "tls_digest_1=10:3c:79:f4:18:9e:78:09:b0:b4:6b:ba:e2:a2:30:71:e9:37:12:16",
        "tls_id_1=C=DE, ST=Nordrhein-Westfalen, L=Koeln, O=CA Company, OU=Organizational Unit, CN=CA, emailAddress=caadmin@company.de",
        "X509_1_emailAddress=caadmin@company.de",
        "X509_1_CN=CA",
        "X509_1_OU=Organizational Unit",
        "X509_1_O=CA Company",
        "X509_1_L=Koeln",
        "X509_1_ST=Nordrhein-Westfalen",
        "X509_1_C=DE",
        "remote_port_1=1194",
        "local_port_1=1194",
        "local_1=192.168.0.100",
        "proto_1=udp",
        "daemon_pid=1748",
        "daemon_start_time=1589703471",
        "daemon_log_redirect=1",
        "daemon=1",
        "verb=2",
        "config=/etc/openvpn/server.conf",
        "ifconfig_local=10.10.91.1",
        "ifconfig_netmask=255.255.255.0",
        "ifconfig_broadcast=10.10.91.255",
        "script_context=init",
        "tun_mtu=1500",
        "link_mtu=1622",
        "dev=tun0",
        "dev_type=tun",
        "redirect_gateway=0"
      };

  @Before
  public void setupUpConfig() throws SharedSecretMissingException, IOException {
    config = new PluginConfiguration();
    config.setNetworkTimeoutLength(5);
    config.setOvpnNasPort(5);
    config.setRadiusAuthPort(1812);
    config.setRadiusAcctPort(1813);
    config.setRadiusIpAddress(
        (IPv4Address) (new IPAddressString("127.0.0.1").getAddress(IPAddress.IPVersion.IPV4)));
    config.setOvpnNasIpAddress(
        (IPv4Address) (new IPAddressString("127.0.0.1").getAddress(IPAddress.IPVersion.IPV4)));
    config.setInternalPluginSessionsDirectory("/tmp/sessions");
    File sessionDirectory = new File(config.getInternalPluginSessionsDirectory());
    if (sessionDirectory.exists() && sessionDirectory.isDirectory()) {
      for (File file : sessionDirectory.listFiles()) {
        Files.deleteIfExists(file.toPath());
      }
      Files.deleteIfExists(sessionDirectory.toPath());
    }
    Files.createDirectory(sessionDirectory.toPath());
    config.setOvpnCcdDirectory("/tmp");
    configNoServer = new PluginConfiguration();
    configNoServer.setNetworkTimeoutLength(5);
    configNoServer.setOvpnNasPort(5);
    configNoServer.setRadiusAuthPort(1814);
    configNoServer.setRadiusAcctPort(1815);
    configNoServer.setRadiusIpAddress(
        (IPv4Address) (new IPAddressString("127.0.0.1").getAddress(IPAddress.IPVersion.IPV4)));
    configNoServer.setOvpnNasIpAddress(
        (IPv4Address) (new IPAddressString("127.0.0.1").getAddress(IPAddress.IPVersion.IPV4)));
    clientInfo = ClientEnvironmentInfo.parseEnvironmentData(clientEnvironmentData);
    accessRequest =
        RadPacket.RadPacketBuilder.createAccessRequestPacket(
            username,
            password,
            sharedSecret,
            (IPv4Address) (new IPAddressString("127.0.0.1").getAddress(IPAddress.IPVersion.IPV4)),
            config.getOvpnNasPort(),
            1);
  }

  @After
  public void deleteTemporaryDirectories() throws IOException {
    File sessionDirectory = new File(config.getInternalPluginSessionsDirectory());
    if (sessionDirectory.exists() && sessionDirectory.isDirectory()) {
      for (File file : sessionDirectory.listFiles()) {
        Files.deleteIfExists(file.toPath());
      }
      Files.deleteIfExists(sessionDirectory.toPath());
    }
  }

  @Test
  public void testWritingConfigFiles() throws IOException {
    RadPacket accessResponse = RadClient.sendReceiveRadiusPacket(accessRequest, config);
    Assert.assertNotNull(accessResponse);
    ClientSessionStore.getInstance()
        .writeClientConfigurationFile(accessResponse, clientInfo, config);
    File routeFile =
        new File(
            config.getOvpnCcdDirectory(),
            clientInfo
                .getEnvironmentMap()
                .get(ClientEnvironmentInfo.ClientEnvironmentAttribute.USERNAME));
    BufferedReader reader = new BufferedReader(new FileReader(routeFile));
    List<String> lines = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line.trim());
    }
    Assert.assertEquals(lines.size(), 4);
    String[] expectedLines =
        new String[] {
          "ifconfig-push 192.168.10.15 255.255.252.0",
          "push \"compress lzo\"",
          "push \"route 192.168.10.0 255.255.255.0 192.168.10.1\"",
          "push \"dhcp-option DNS 192.168.0.100\""
        };
    for (String expectedLine : expectedLines) {
      Assert.assertTrue(lines.contains(expectedLine));
    }
  }

  @Test
  public void testAccessRequest() throws IOException, ResponseValidationException {
    RadPacket accessResponse = RadClient.sendReceiveRadiusPacket(accessRequest, config);
    boolean validation =
        RadPacket.validateRequestAndResponse(accessRequest, accessResponse, sharedSecret);
    Assert.assertTrue(validation);
  }

  @Test
  public void testAccountingRequests()
      throws SharedSecretMissingException, ResponseValidationException, IOException {
    String sessionId = UUID.randomUUID().toString();

    RadPacket acctStart =
        RadPacket.RadPacketBuilder.createAccountingRequestPacket(
            username,
            sharedSecret,
            config.getOvpnNasIpAddress(),
            config.getOvpnNasPort(),
            RadAttribute.RadAccountingStatusType.START,
            (IPv4Address)
                (new IPAddressString("192.168.100.10").getAddress(IPAddress.IPVersion.IPV4)),
            "74.208.14.77:12345",
            0,
            0,
            0,
            sessionId,
            RadAttribute.RadAccountingAuthenticationMode.RADIUS,
            0,
            0,
            0,
            RadAttribute.RadAccountingTerminationCause.IGNORE,
            1);
    RadPacket acctInterim =
        RadPacket.RadPacketBuilder.createAccountingRequestPacket(
            username,
            sharedSecret,
            config.getOvpnNasIpAddress(),
            config.getOvpnNasPort(),
            RadAttribute.RadAccountingStatusType.INTERIM_ACCT,
            (IPv4Address)
                (new IPAddressString("192.168.100.10").getAddress(IPAddress.IPVersion.IPV4)),
            "74.208.14.77:12345",
            0,
            10,
            20,
            sessionId,
            RadAttribute.RadAccountingAuthenticationMode.RADIUS,
            15,
            50,
            60,
            RadAttribute.RadAccountingTerminationCause.IGNORE,
            2);
    RadPacket acctStop =
        RadPacket.RadPacketBuilder.createAccountingRequestPacket(
            username,
            sharedSecret,
            config.getOvpnNasIpAddress(),
            config.getOvpnNasPort(),
            RadAttribute.RadAccountingStatusType.STOP,
            (IPv4Address)
                (new IPAddressString("192.168.100.10").getAddress(IPAddress.IPVersion.IPV4)),
            "74.208.14.77:12345",
            0,
            30,
            40,
            sessionId,
            RadAttribute.RadAccountingAuthenticationMode.RADIUS,
            25,
            70,
            80,
            RadAttribute.RadAccountingTerminationCause.USER_REQUEST,
            3);
    RadPacket startResponse = RadClient.sendReceiveRadiusPacket(acctStart, config);
    RadPacket interimResponse = RadClient.sendReceiveRadiusPacket(acctInterim, config);
    RadPacket stopResponse = RadClient.sendReceiveRadiusPacket(acctStop, config);
    boolean startValidation =
        RadPacket.validateRequestAndResponse(acctStart, startResponse, sharedSecret);
    boolean interimValidation =
        RadPacket.validateRequestAndResponse(acctInterim, interimResponse, sharedSecret);
    boolean stopValidation =
        RadPacket.validateRequestAndResponse(acctStop, stopResponse, sharedSecret);
    Assert.assertTrue(startValidation);
    Assert.assertTrue(interimValidation);
    Assert.assertTrue(stopValidation);
  }

  @Test
  public void testAccessRequestNonExistentRadius() throws SharedSecretMissingException {
    Assert.assertThrows(
        SocketTimeoutException.class,
        () -> {
          RadClient.sendReceiveRadiusPacket(accessRequest, configNoServer);
        });
  }
}
