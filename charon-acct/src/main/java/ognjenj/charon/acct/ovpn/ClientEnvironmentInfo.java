package ognjenj.charon.acct.ovpn;

import java.util.EnumMap;

public class ClientEnvironmentInfo {

	private final EnumMap<ClientEnvironmentAttribute, String> environmentMap = new EnumMap<>(
			ClientEnvironmentAttribute.class);

	public static ClientEnvironmentInfo parseEnvironmentData(String[] attributeKeyValuePairs) {
		ClientEnvironmentInfo clientInfo = new ClientEnvironmentInfo();
		for (String attributeKeyValue : attributeKeyValuePairs) {
			String[] keyValue = attributeKeyValue.split("=");
			for (ClientEnvironmentAttribute attributeName : ClientEnvironmentAttribute.values()) {
				if (keyValue[0].equals(attributeName.getAttributeName())) {
					clientInfo.getEnvironmentMap().put(attributeName, keyValue[1].trim());
					break;
				}
			}
		}
		return clientInfo;
	}

	public EnumMap<ClientEnvironmentAttribute, String> getEnvironmentMap() {
		return environmentMap;
	}

	public enum ClientEnvironmentAttribute {
		COMMON_NAME("common_name"), CONTROL_FILE("auth_control_file"), UNTRUSTED_PORT("untrusted_port"), UNTRUSTED_IP(
				"untrusted_ip"), PASSWORD("password"), USERNAME("username"), CLIENT_DN("tls_id_0"), CERT_EMAIL(
						"X509_0_emailAddress"), CERT_COMMON_NAME(
								"X509_0_CN"), CERT_ORGANIZATIONAL_UNIT("X509_0_OU"), CERT_ORGANIZATION(
										"X509_0_O"), CERT_LOCATION("X509_0_L"), CERT_COUNTRY("X509_0_C"), CERT_SERIAL(
												"tls_serial_0"), OPENVPN_IP_ADDRESS("local_1"), OPENVPN_PORT(
														"local_port_1"), OPENVPN_GATEWAY("ifconfig_local"), BYTES_SENT(
																"bytes_sent"), BYTES_RECEIVED("bytes_received");

		private final String attributeName;
		ClientEnvironmentAttribute(String attributeName) {
			this.attributeName = attributeName;
		}

		public String getAttributeName() {
			return attributeName;
		}
	}
}
