package ognjenj.charon.web.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Crypt;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SaltGenerator {
	public static String getAlphaNumericString(int stringSize) {
		String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(stringSize);
		for (int i = 0; i < stringSize; i++) {
			int index = (int) (alphaNumericString.length() * Math.random());
			sb.append(alphaNumericString.charAt(index));
		}
		return sb.toString();
	}

	public static String generatePasswordWithRandomSalt(String originalPassword) {
		return Crypt.crypt(originalPassword, "$6$" + SaltGenerator.getAlphaNumericString(8) + "$");
	}

	public static String generateSha512asHex(String originalPassword) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] messageDigest = md.digest(originalPassword.getBytes());
		return Hex.encodeHexString(messageDigest);
	}
}
