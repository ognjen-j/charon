package ognjenj.charon.acct.radius;

public enum RadAttributeType {
	USER_NAME(1, 0, true), USER_PASSWORD(2, 0, true), CHAP_PASSWORD(3, 19, false), NAS_IP_ADDRESS(4, 6,
			false), NAS_PORT(5, 6, false), SERVICE_TYPE(6, 6, false), FRAMED_PROTOCOL(7, 6, false), FRAMED_IP_ADDRESS(8,
					6, false), FRAMED_IP_NETMASK(9, 6, false), FRAMED_ROUTING(10, 6, false), FILTER_ID(11, 0,
							true), FRAMED_MTU(12, 6, false), FRAMED_COMPRESSION(13, 6, false), LOGIN_IP_HOST(14, 6,
									false), LOGIN_SERVICE(15, 6, false), LOGIN_TCP_PORT(16, 6, false), UNASSIGNED(17, 0,
											true), REPLY_MESSAGE(18, 0, true), CALLBACK_NUMBER(19, 0,
													true), CALLBACK_ID(20, 0, true), UNASSIGNED_2(21, 0,
															true), FRAMED_ROUTE(22, 0, true), FRAMED_IPX_NETWORK(23, 6,
																	false), STATE(24, 0, true), CLASS(25, 0,
																			true), VENDOR_SPECIFIC(26, 0,
																					true), SESSION_TIMEOUT(27, 6,
																							false), IDLE_TIMEOUT(28, 6,
																									false), TERMINATION_ACTION(
																											29, 6,
																											false), CALLED_STATION_ID(
																													30,
																													0,
																													true), CALLING_STATION_ID(
																															31,
																															0,
																															true), NAS_IDENTIFIER(
																																	32,
																																	0,
																																	true), PROXY_STATE(
																																			33,
																																			0,
																																			true), LOGIN_LAT_SERVICE(
																																					34,
																																					0,
																																					true), LOGIN_LAT_NODE(
																																							35,
																																							0,
																																							true), LOGIN_LAT_GROUP(
																																									36,
																																									34,
																																									false), FRAMED_APPLETALK_LINK(
																																											37,
																																											6,
																																											false), FRAMED_APPLETALK_NETWORK(
																																													38,
																																													6,
																																													false), FRAMED_APPLETALK_ZONE(
																																															39,
																																															0,
																																															true), ACCT_STATUS_TYPE(
																																																	40,
																																																	6,
																																																	false), ACCT_DELAY_TIME(
																																																			41,
																																																			6,
																																																			false), ACCT_INPUT_OCTETS(
																																																					42,
																																																					6,
																																																					false), ACCT_OUTPUT_OCTETS(
																																																							43,
																																																							6,
																																																							false), ACCT_SESSION_ID(
																																																									44,
																																																									0,
																																																									true), ACCT_AUTHENTIC(
																																																											45,
																																																											6,
																																																											false), ACCT_SESSION_TIME(
																																																													46,
																																																													6,
																																																													false), ACCT_INPUT_PACKETS(
																																																															47,
																																																															6,
																																																															false), ACCT_OUTPUT_PACKETS(
																																																																	48,
																																																																	6,
																																																																	false), ACCT_TERMINATE_CAUSE(
																																																																			49,
																																																																			6,
																																																																			false), ACCT_MULTI_SESSION_ID(
																																																																					50,
																																																																					0,
																																																																					true), ACCT_LINK_COUNT(
																																																																							51,
																																																																							6,
																																																																							false), ACCT_INPUT_GIGAWORDS(
																																																																									52,
																																																																									6,
																																																																									false), ACCT_OUTPUT_GIGAWORDS(
																																																																											53,
																																																																											6,
																																																																											false), CHAP_CHALLENGE(
																																																																													60,
																																																																													0,
																																																																													true), NAS_PORT_TYPE(
																																																																															61,
																																																																															6,
																																																																															false), PORT_LIMIT(
																																																																																	62,
																																																																																	6,
																																																																																	false), LOGIN_LAT_PORT(
																																																																																			63,
																																																																																			0,
																																																																																			true), MESSAGE_AUTHENTICATOR(
																																																																																					80,
																																																																																					18,
																																																																																					false), FRAMED_IPV6_ADDRESS(
																																																																																							168,
																																																																																							18,
																																																																																							false), DNS_IPV6_ADDRESS(
																																																																																									169,
																																																																																									18,
																																																																																									false);

	private final int code;
	private final int length;
	private final boolean lengthVariable;

	RadAttributeType(int code, int length, boolean lengthVariable) {
		this.code = code;
		this.length = length;
		this.lengthVariable = lengthVariable;
	}

	public static RadAttributeType getTypeForValue(int code) {
		for (RadAttributeType type : RadAttributeType.values()) {
			if (type.getCode() == code)
				return type;
		}
		return null;
	}

	public int getCode() {
		return code;
	}

	public int getLength() {
		return length;
	}

	public boolean isLengthVariable() {
		return lengthVariable;
	}
}