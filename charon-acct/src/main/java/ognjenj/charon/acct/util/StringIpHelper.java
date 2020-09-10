package ognjenj.charon.acct.util;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

public class StringIpHelper {
	public static final String DEFAULT_ROUTE = "0.0.0.0/0";
	/**
	 * Transforms the route from x.x.x.x/y z.z.z.z -> x.x.x.x y.y.y.y z.z.z.z, where
	 * x.x.x.x is the network address, y.y.y.y is the netmask and z.z.z.z is the
	 * gateway
	 *
	 * @param originalRoute
	 * @return
	 */
	public static String transformRouteNotation(String originalRoute) {
		try {
			if (originalRoute.equals(DEFAULT_ROUTE))
				return originalRoute;
			String[] networkAndGw = originalRoute.split(" ");
			String[] networkAndMask = networkAndGw[0].split("/");
			int maskLength = Integer.parseInt(networkAndMask[1]);
			IPAddress address = new IPAddressString(networkAndMask[0]).getAddress();
			IPAddress mask = address.getNetwork().getNetworkMask(maskLength, false);
			IPAddress gateway = new IPAddressString(networkAndGw[1]).getAddress();
			return String.format("%s %s %s", address.mask(mask).toConvertedString(), mask.toConvertedString(),
					gateway.toConvertedString());
		} catch (NumberFormatException ex) {
			return "0.0.0.0 0.0.0.0 0.0.0.0";
		}
	}
}
