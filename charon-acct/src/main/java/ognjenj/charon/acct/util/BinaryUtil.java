package ognjenj.charon.acct.util;

public class BinaryUtil {

	public static byte[] longToBinaryArray(long value, int byteArrayLength, boolean networkOrder) {
		byte[] result = new byte[byteArrayLength];
		long currentInt = value;
		for (int cnt = 0; cnt < byteArrayLength; cnt++) {
			byte currentByte = (byte) (currentInt & 0xff);
			result[networkOrder ? byteArrayLength - cnt - 1 : cnt] = currentByte;
			currentInt = currentInt >> 8;
		}
		return result;
	}
}
