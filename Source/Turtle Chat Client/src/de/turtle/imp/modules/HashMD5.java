package de.turtle.imp.modules;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashMD5 {
	public static String createHash(String password)
			throws NoSuchAlgorithmException {
		String hashedPassword = "";
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte[] byteData = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		hashedPassword = (String) sb.toString();
		return hashedPassword;
	}

}