package ognjenj.charon.acct.radius;

import static ognjenj.charon.acct.radius.RadAttributeType.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import inet.ipaddr.ipv4.IPv4Address;
import ognjenj.charon.acct.exceptions.MalformedPacketException;
import ognjenj.charon.acct.exceptions.ResponseValidationException;
import ognjenj.charon.acct.exceptions.SharedSecretMissingException;
import ognjenj.charon.acct.util.BinaryUtil;
import ognjenj.charon.acct.util.PasswordUtil;

public class RadPacket implements Serializable {
	private final List<RadAttribute> attributes = new ArrayList<>();
	private RadPacketType packetType;
	private byte packetIdentifier;
	private int packetLength;
	private byte[] authenticator = new byte[16];
	private String sharedSecret;

	private RadPacket() {
		this.packetType = RadPacketType.ACCESS_REQUEST;
	}

	public static RadPacket constructFromBinaryFormat(byte[] rawData) throws MalformedPacketException {
		RadPacket result = new RadPacket();
		result.packetType = RadPacketType.getTypeForValue(rawData[0]);
		result.packetIdentifier = rawData[1];
		result.packetLength = (rawData[2] << 8) + rawData[3];
		result.authenticator = new byte[16];
		System.arraycopy(rawData, 4, result.authenticator, 0, 16);
		int currentPosition = 20;
		while (currentPosition < result.packetLength) {
			byte attributeType = rawData[currentPosition];
			byte attributeLength = rawData[currentPosition + 1];
			byte[] attributeContent = new byte[attributeLength - 2];
			System.arraycopy(rawData, currentPosition + 2, attributeContent, 0, attributeLength - 2);
			RadAttribute attribute = new RadAttribute(RadAttributeType.getTypeForValue(attributeType),
					attributeContent);
			result.getAttributes().add(attribute);
			currentPosition += attributeLength;
		}
		return result;
	}

	public static boolean validateRequestAndResponse(RadPacket request, RadPacket response, String sharedSecret)
			throws ResponseValidationException {
		if (response == null) {
			throw new ResponseValidationException("Invalid response: null");
		}
		if (request.getPacketIdentifier() != response.getPacketIdentifier()) {
			throw new ResponseValidationException("Invalid response: packet identifiers don't match.");
		}
		byte[] sharedSecretBytes = sharedSecret.getBytes(StandardCharsets.UTF_8);
		byte[] responsePadded = new byte[response.packetLength + sharedSecretBytes.length];
		byte[] convertedResponse = response.convertToNetworkReadyFormat();
		System.arraycopy(convertedResponse, 0, responsePadded, 0, response.packetLength);
		System.arraycopy(sharedSecretBytes, 0, responsePadded, response.packetLength, sharedSecretBytes.length);
		System.arraycopy(request.authenticator, 0, responsePadded, 4, 16);
		byte[] authenticatorHash = PasswordUtil.calculateMd5(responsePadded);

		return Arrays.equals(authenticatorHash, response.authenticator);
	}

	public byte getPacketIdentifier() {
		return packetIdentifier;
	}

	public int getPacketLength() {
		return packetLength;
	}

	public RadPacketType getPacketType() {
		return packetType;
	}

	public List<RadAttribute> getAttributes() {
		return attributes;
	}

	public byte[] convertToNetworkReadyFormat() {
		byte[] content = new byte[this.packetLength];
		content[0] = (byte) this.packetType.packetTypeId;
		content[1] = this.packetIdentifier;
		content[2] = (byte) (this.packetLength >> 8);
		content[3] = (byte) (this.packetLength);
		System.arraycopy(this.authenticator, 0, content, 4, 16);
		int currentPosition = 20;
		for (int cnt = 0; cnt < this.attributes.size(); cnt++) {
			RadAttribute attribute = this.attributes.get(cnt);
			content[currentPosition] = (byte) attribute.getAttributeType().getCode();
			content[currentPosition + 1] = (byte) attribute.getTotalLength();
			System.arraycopy(attribute.getAttributeValue(), 0, content, currentPosition + 2,
					attribute.getTotalLength() - 2);
			currentPosition += attribute.getTotalLength();
		}
		return content;
	}

	@SuppressWarnings("unused")
	public enum RadPacketType {
		ACCESS_REQUEST(1), ACCESS_ACCEPT(2), ACCESS_REJECT(3), ACCOUNTING_REQUEST(4), ACCOUNTING_RESPONSE(
				5), ACCOUNTING_STATUS(6), PASSWORD_REQUEST(7), PASSWORD_ACK(8), PASSWORD_REJECT(9), ACCOUNTING_MESSAGE(
						10), ACCESS_CHALLENGE(11), STATUS_SERVER(12), STATUS_CLIENT(13), RESOURCE_FREE_REQUEST(
								21), RESOURCE_FREE_RESPONSE(22), RESOURCE_QUERY_REQUEST(23), RESOURCE_QUERY_RESPONSE(
										24), ALTERNATE_RESOURCE_RECLAIM_REQUEST(25), NAS_REBOOT_REQUEST(
												26), NAS_REBOOT_RESPONSE(27), RESERVED(28), NEXT_PASSCODE(29), NEW_PIN(
														30), TERMINATE_SESSION(31), PASSWORD_EXPIRED(32), EVENT_REQUEST(
																33), EVENT_RESPONSE(
																		34), DISCONNECT_REQUEST(40), DISCONNECT_ACK(
																				41), DISCONNECT_NAK(42), COA_REQUEST(
																						43), COA_ACK(44), COA_NAK(
																								45), IP_ADDRESS_ALLOCATE(
																										50), IP_ADDRESS_RELEASE(
																												51), PROTOCOL_ERROR(
																														52);

		private final int packetTypeId;

		RadPacketType(int packetTypeId) {
			this.packetTypeId = packetTypeId;
		}

		public static RadPacketType getTypeForValue(int packetTypeId) {
			for (RadPacketType type : RadPacketType.values()) {
				if (type.getPacketTypeId() == packetTypeId)
					return type;
			}
			return null;
		}

		public int getPacketTypeId() {
			return packetTypeId;
		}
	}

	public static class RadPacketBuilder {

		private RadPacket packet;

		private RadPacketBuilder() {
			packet = new RadPacket();
		}

		public static RadPacketBuilder createInstance() {
			return new RadPacketBuilder();
		}

		public static RadPacket createAccessRequestPacket(String username, String password, String sharedSecret,
				IPv4Address nasIpAddress, int radiusNasPort, int packetId) throws SharedSecretMissingException {
			byte[] encryptedPassword;
			byte[] requestAuthenticator = new byte[16];
			new Random().nextBytes(requestAuthenticator);
			RadAttributeType passwordAttributeType;

			encryptedPassword = PasswordUtil.calculatePapPassword(password, sharedSecret, requestAuthenticator);
			passwordAttributeType = USER_PASSWORD;
			return RadPacketBuilder.createInstance().setStringAttribute(USER_NAME, username)
					.setByteArrayAttribute(passwordAttributeType, encryptedPassword)
					.setAttribute(NAS_IP_ADDRESS, nasIpAddress, e -> ((IPv4Address) e).getBytes())
					.setAttribute(NAS_PORT, radiusNasPort,
							e -> BinaryUtil.longToBinaryArray(radiusNasPort, NAS_PORT.getLength() - 2, true))
					.setByteArrayAttribute(MESSAGE_AUTHENTICATOR, new byte[MESSAGE_AUTHENTICATOR.getLength() - 2])
					.setPacketIdentifier((byte) packetId).setAuthenticator(requestAuthenticator)
					.setPacketType(RadPacketType.ACCESS_REQUEST).setSharedSecret(sharedSecret).build();
		}

		public static RadPacket createAccountingRequestPacket(String username, String sharedSecret,
				IPv4Address nasIpAddress, int radiusNasPort, RadAttribute.RadAccountingStatusType statusType,
				IPv4Address framedIpAddress, String callingStationId, long acctDelayTime, long acctInputOctets,
				long acctOutputOctets, String sessionId, RadAttribute.RadAccountingAuthenticationMode authMode,
				int acctSessionTime, int acctInputPackets, int acctOutputPackets,
				RadAttribute.RadAccountingTerminationCause terminationCause, int packetId)
				throws SharedSecretMissingException {

			int inputGigawords = (int) (acctInputOctets / 4294967295L);
			int remainingInputOctets = (int) (acctInputOctets % 4294967295L);
			int outputGigawords = (int) (acctOutputOctets / 4294967295L);
			int remainingOutputOctets = (int) (acctOutputOctets % 4294967295L);
			RadPacketBuilder packetBuilder = RadPacketBuilder.createInstance().setStringAttribute(USER_NAME, username)
					.setAttribute(NAS_IP_ADDRESS, nasIpAddress, e -> ((IPv4Address) e).getBytes())
					.setAttribute(NAS_PORT, radiusNasPort,
							e -> BinaryUtil.longToBinaryArray((int) e, NAS_PORT.getLength() - 2, true))
					.setAttribute(ACCT_STATUS_TYPE, statusType.getValue(),
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_STATUS_TYPE.getLength() - 2, true))
					.setAttribute(FRAMED_IP_ADDRESS, framedIpAddress, e -> ((IPv4Address) e).getBytes())
					.setStringAttribute(CALLING_STATION_ID, callingStationId)
					.setAttribute(ACCT_DELAY_TIME, acctDelayTime,
							e -> BinaryUtil.longToBinaryArray((long) e, ACCT_DELAY_TIME.getLength() - 2, true))
					.setAttribute(ACCT_INPUT_OCTETS, remainingInputOctets,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_INPUT_OCTETS.getLength() - 2, true))
					.setAttribute(ACCT_OUTPUT_OCTETS, remainingOutputOctets,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_OUTPUT_OCTETS.getLength() - 2, true))
					.setAttribute(ACCT_INPUT_GIGAWORDS, inputGigawords,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_INPUT_GIGAWORDS.getLength() - 2, true))
					.setAttribute(ACCT_OUTPUT_GIGAWORDS, outputGigawords,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_OUTPUT_GIGAWORDS.getLength() - 2, true))
					.setStringAttribute(ACCT_SESSION_ID, sessionId)
					.setAttribute(ACCT_AUTHENTIC, authMode.getValue(),
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_AUTHENTIC.getLength() - 2, true))
					.setAttribute(ACCT_SESSION_TIME, acctSessionTime,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_SESSION_TIME.getLength() - 2, true))
					.setAttribute(ACCT_INPUT_PACKETS, acctInputPackets,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_INPUT_PACKETS.getLength() - 2, true))
					.setAttribute(ACCT_OUTPUT_PACKETS, acctOutputPackets,
							e -> BinaryUtil.longToBinaryArray((int) e, ACCT_OUTPUT_PACKETS.getLength() - 2, true))
					.setPacketIdentifier((byte) packetId).setSharedSecret(sharedSecret)
					.setPacketType(RadPacketType.ACCOUNTING_REQUEST).setAuthenticator(new byte[16]);
			if (terminationCause != RadAttribute.RadAccountingTerminationCause.IGNORE) {
				packetBuilder = packetBuilder.setAttribute(ACCT_TERMINATE_CAUSE, terminationCause.getValue(),
						e -> BinaryUtil.longToBinaryArray((int) e, ACCT_TERMINATE_CAUSE.getLength() - 2, true));
			}
			return packetBuilder.build();
		}

		public RadPacketBuilder setAttribute(RadAttributeType attributeType, Object value,
				RadAttribute.AttributeValueConverter valueConverter) {
			this.packet.getAttributes().add(new RadAttribute(attributeType, value, valueConverter));
			return this;
		}

		public RadPacketBuilder setStringAttribute(RadAttributeType attributeType, String value) {
			this.packet.getAttributes().add(new RadAttribute(attributeType, value));
			return this;
		}

		public RadPacketBuilder setByteArrayAttribute(RadAttributeType attributeType, byte[] value) {
			this.packet.getAttributes().add(new RadAttribute(attributeType, value));
			return this;
		}

		public RadPacketBuilder setPacketType(RadPacketType packetType) {
			this.packet.packetType = packetType;
			return this;
		}

		public RadPacketBuilder setPacketIdentifier(byte packetIdentifier) {
			this.packet.packetIdentifier = packetIdentifier;
			return this;
		}

		public RadPacketBuilder setAuthenticator(byte[] authenticator) {
			this.packet.authenticator = authenticator;
			return this;
		}

		public RadPacketBuilder setSharedSecret(String sharedSecret) {
			this.packet.sharedSecret = sharedSecret;
			return this;
		}

		public RadPacket build() throws SharedSecretMissingException {
			if (this.packet.sharedSecret == null) {
				throw new SharedSecretMissingException(
						"You must provide a shared secret in order to assemble the Radius packet.");
			}
			this.packet.packetLength = this.packet.getAttributes().stream().mapToInt(RadAttribute::getTotalLength)
					.reduce(0, Integer::sum) + 20;
			byte[] nonSignedContent = this.packet.convertToNetworkReadyFormat();
			byte[] sharedSecretBytes = this.packet.sharedSecret.getBytes(StandardCharsets.UTF_8);
			if (this.packet.packetType == RadPacketType.ACCESS_REQUEST) {
				byte[] signature = PasswordUtil.calculateMd5HMAC(nonSignedContent, sharedSecretBytes);
				RadAttribute newAuthenticator = new RadAttribute(MESSAGE_AUTHENTICATOR, signature, e -> (byte[]) e);
				int index = this.packet.attributes.indexOf(newAuthenticator);
				this.packet.attributes.set(index, newAuthenticator);
			} else if (this.packet.packetType == RadPacketType.ACCOUNTING_REQUEST) {
				byte[] paddedRequest = new byte[nonSignedContent.length + sharedSecretBytes.length];
				System.arraycopy(nonSignedContent, 0, paddedRequest, 0, nonSignedContent.length);
				System.arraycopy(sharedSecretBytes, 0, paddedRequest, nonSignedContent.length,
						sharedSecretBytes.length);
				this.packet.authenticator = PasswordUtil.calculateMd5(paddedRequest);
			}
			return this.packet;
		}
	}
}
