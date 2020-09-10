package ognjenj.charon.acct.radius;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class RadAttribute implements Serializable {
	private final RadAttributeType attributeType;
	private byte[] attributeValue;
	private int totalLength = 0;
	private RadAttributeVendorSpecificInfo vendorSpecificInfo;

	RadAttribute(RadAttributeType attributeType, byte[] attributeValue) {
		this(attributeType, attributeValue, value -> (byte[]) value);
	}

	RadAttribute(RadAttributeType attributeType, String attributeValue) {
		this(attributeType, attributeValue, value -> attributeValue.getBytes(StandardCharsets.UTF_8));
	}

	RadAttribute(RadAttributeType attributeType, Object attributeValue, AttributeValueConverter valueConverter) {
		this.attributeType = attributeType;
		this.attributeValue = valueConverter.convertToBinaryForm(attributeValue);
		this.totalLength = this.attributeType.isLengthVariable()
				? this.attributeValue.length + 2
				: this.attributeType.getLength();
		this.vendorSpecificInfo = RadAttributeVendorSpecificInfo.parse(attributeType, this.attributeValue);
	}

	public RadAttributeVendorSpecificInfo getVendorSpecificInfo() {
		return vendorSpecificInfo;
	}

	public int getTotalLength() {
		return totalLength;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RadAttribute that = (RadAttribute) o;
		return attributeType == that.attributeType;
	}

	public void zeroizeValue() {
		this.attributeValue = new byte[this.totalLength - 2];
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributeType);
	}

	public RadAttributeType getAttributeType() {
		return attributeType;
	}

	public byte[] getAttributeValue() {
		return attributeValue;
	}

	public byte[] getVendorSpecificAttributeValue() {
		if (vendorSpecificInfo != null) {
			byte[] vendorSpecificValue = new byte[totalLength - 8];
			System.arraycopy(attributeValue, 6, vendorSpecificValue, 0, totalLength - 8);
			return vendorSpecificValue;
		}
		return null;
	}

	@SuppressWarnings("unused")
	public enum RadAccountingStatusType {
		START(1), STOP(2), INTERIM_ACCT(3), ACCOUNTING_ON(7), ACCOUNTING_OFF(8);

		private final int value;

		RadAccountingStatusType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	@SuppressWarnings("unused")
	public enum RadAccountingAuthenticationMode {
		RADIUS(1), LOCAL(2), REMOTE(3);

		private final int value;

		RadAccountingAuthenticationMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	@SuppressWarnings("unused")
	public enum RadAccountingTerminationCause {
		USER_REQUEST(1), LOST_CARRIER(2), LOST_SERVICE(3), IDLE_TIMEOUT(4), SESSION_TIMEOUT(5), ADMIN_RESET(
				6), ADMIN_REBOOT(7), PORT_ERROR(8), NAS_ERROR(9), NAS_REQUEST(10), NAS_REBOOT(11), PORT_UNNEEDED(
						12), PORT_PREEMPTED(13), PORT_SUSPENDED(14), SERVICE_UNAVAILABLE(
								15), CALLBACK(16), USER_ERROR(17), HOST_REQUESTED(18), IGNORE(99);

		private final int value;

		RadAccountingTerminationCause(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	@SuppressWarnings("unused")
	public enum RadAuthenticationProtocol {
		PAP, CHAP
	}

	public interface AttributeValueConverter {
		byte[] convertToBinaryForm(Object originalValue);
	}

	public static class RadAttributeVendorSpecificInfo {
		private RadAttributeVendor vendor;
		private RadAttributeVendor.RadVendorSpecificAttribute attributeType;
		private byte[] attributeValue;

		public static RadAttributeVendorSpecificInfo parse(RadAttributeType basicType, byte[] fullAttributeContent) {
			if (basicType == RadAttributeType.VENDOR_SPECIFIC) {
				long vendorId = (fullAttributeContent[0] << 24) + (fullAttributeContent[1] << 16)
						+ (fullAttributeContent[2] << 8) + (fullAttributeContent[3]);
				RadAttributeVendor vendor = RadAttributeVendor.vendorForValue(vendorId);
				if (vendor != null) {
					int vendorSpecificAttributeType = fullAttributeContent[4];
					int vendorSpecificAttributeLength = fullAttributeContent[5];
					Optional<RadAttributeVendor.RadVendorSpecificAttribute> subType = Arrays
							.stream(vendor.getAttributes())
							.filter(e -> e.getVendorSpecificAttributeId() == vendorSpecificAttributeType).findFirst();
					if (subType.isEmpty())
						return null;
					else {
						RadAttributeVendorSpecificInfo info = new RadAttributeVendorSpecificInfo();
						info.attributeType = subType.get();
						info.vendor = vendor;
						info.attributeValue = new byte[vendorSpecificAttributeLength - 2];
						System.arraycopy(fullAttributeContent, 6, info.attributeValue, 0,
								vendorSpecificAttributeLength - 2);
						return info;
					}
				}
				return null;
			}
			return null;
		}

		public RadAttributeVendor getVendor() {
			return vendor;
		}

		public RadAttributeVendor.RadVendorSpecificAttribute getAttributeType() {
			return attributeType;
		}

		public byte[] getAttributeValue() {
			return attributeValue;
		}
	}
}
