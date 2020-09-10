package ognjenj.charon.acct.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordUtil {
	public static byte[] calculatePapPassword(String password, String sharedSecret, byte[] requestAuthenticator) {
		try {
			byte[] originalPasswordBytes = password.getBytes(StandardCharsets.US_ASCII);
			byte[] sharedSecretBytes = sharedSecret.getBytes(StandardCharsets.US_ASCII);
			int papPasswordLength = originalPasswordBytes.length % 16 == 0
					? originalPasswordBytes.length
					: (originalPasswordBytes.length / 16 + 1) * 16;
			byte[] paddedOriginalPasswordBytes = new byte[papPasswordLength];
			System.arraycopy(originalPasswordBytes, 0, paddedOriginalPasswordBytes, 0, originalPasswordBytes.length);
			byte[] papPassword = new byte[papPasswordLength];
			System.arraycopy(originalPasswordBytes, 0, papPassword, 0, originalPasswordBytes.length);
			int chunkCount = paddedOriginalPasswordBytes.length / 16;
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] singleChunk = new byte[16];
			for (int cnt = 0; cnt < chunkCount; cnt++) {
				byte[] hashed_authenticator;
				byte[] authenticator = new byte[sharedSecretBytes.length + requestAuthenticator.length];
				System.arraycopy(sharedSecretBytes, 0, authenticator, 0, sharedSecretBytes.length);
				if (cnt == 0) {
					System.arraycopy(requestAuthenticator, 0, authenticator, sharedSecretBytes.length, 16);
				} else {
					System.arraycopy(papPassword, (cnt - 1) * 16, authenticator, sharedSecretBytes.length, 16);
				}
				digest.update(authenticator);
				hashed_authenticator = digest.digest();
				for (int chunkCnt = cnt * 16, passCnt = 0; chunkCnt < (cnt + 1) * 16; chunkCnt++, passCnt++) {
					singleChunk[passCnt] = (byte) (hashed_authenticator[passCnt]
							^ paddedOriginalPasswordBytes[chunkCnt]);
				}
				System.arraycopy(singleChunk, 0, papPassword, cnt * 16, 16);
			}
			return papPassword;
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}

	public static byte[] calculateMd5HMAC(byte[] content, byte[] secret) {
		try {
			Key key = new SecretKeySpec(secret, "HMACMD5");
			Mac mac = Mac.getInstance("HMACMD5");
			mac.init(key);
			return mac.doFinal(content);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}

	public static byte[] calculateMd5(byte[] content) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(content);
			return digest.digest();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}
}
