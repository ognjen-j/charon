package ognjenj.charon.acct.radius;

public enum RadAttributeVendor {
	RAD_VENDOR_MICROSOFT(311,
			new RadVendorSpecificAttribute[]{RadVendorSpecificAttribute.RAD_VENDOR_MICROSOFT_PRIMARY_DNS,
					RadVendorSpecificAttribute.RAD_VENDOR_MICROSOFT_SECONDARY_DNS});

	private final long vendorId;
	private final RadVendorSpecificAttribute[] attributes;

	RadAttributeVendor(long vendorId, RadVendorSpecificAttribute[] attributes) {
		this.vendorId = vendorId;
		this.attributes = attributes;
	}

	public static RadAttributeVendor vendorForValue(long vendorId) {
		for (RadAttributeVendor vendor : RadAttributeVendor.values()) {
			if (vendor.getVendorId() == vendorId)
				return vendor;
		}
		return null;
	}

	public long getVendorId() {
		return vendorId;
	}

	public RadVendorSpecificAttribute[] getAttributes() {
		return attributes;
	}

	public enum RadVendorSpecificAttribute {
		RAD_VENDOR_MICROSOFT_PRIMARY_DNS(28), RAD_VENDOR_MICROSOFT_SECONDARY_DNS(29);

		private int vendorSpecificAttributeId;

		RadVendorSpecificAttribute(int vendorSpecificAttributeId) {
			this.vendorSpecificAttributeId = vendorSpecificAttributeId;
		}

		public static RadVendorSpecificAttribute vendorSpecificAttributeForValue(int vendorSpecificAttributeId) {
			for (RadVendorSpecificAttribute vendorSpecificAttribute : RadVendorSpecificAttribute.values()) {
				if (vendorSpecificAttribute.getVendorSpecificAttributeId() == vendorSpecificAttributeId)
					return vendorSpecificAttribute;
			}
			return null;
		}

		public int getVendorSpecificAttributeId() {
			return vendorSpecificAttributeId;
		}
	}
}
