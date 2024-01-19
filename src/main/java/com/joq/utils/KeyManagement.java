package com.joq.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class KeyManagement {

	/**
	 * Method to check the expire of the execution
	 * 
	 * @author sanoj.swaminathan
	 * @since 15-12-2023
	 * @return
	 * @throws Exception
	 */
	public static boolean checkExpiry() throws Exception {
		boolean isExpired = false;
		Properties props = KeyManagement.readAndDecryptProperties();
		int year = Integer.parseInt(props.get("year").toString());
		int month = Integer.parseInt(props.get("month").toString());
		int dayOfMonth = Integer.parseInt(props.get("day").toString());

		LocalDate currentDate = LocalDate.now();
		LocalDate expirationDate = LocalDate.of(year, month, dayOfMonth);
		long daysUntilExpiration = ChronoUnit.DAYS.between(currentDate, expirationDate);

		if (daysUntilExpiration >= 0) {
			isExpired = true;
			System.out.println("Welcome to the automation execution...");
		} else {
			isExpired = false;
			System.out.println("Expired. Please contact the author.");
			System.exit(0);
		}
		return isExpired;
	}

	/**
	 * Method to read and decrypt the key
	 * 
	 * @author sanoj.swaminthan
	 * @since 15-12-2023
	 * @param encodedKey
	 * @return
	 * @throws Exception
	 */
	private static Properties readAndDecryptProperties() throws Exception {

		Properties decryptedProperties;
		Properties decryptKeyProperties = null;

		try {
			String currentSecretKeyFilePath = System.getProperty("user.dir") + "./src/main/resources/Key/"
					+ "secret_key.key";
			File currentSecretKeyFile = new File(currentSecretKeyFilePath);
			String proposedSecretKeyFilePath = System.getProperty("user.dir") + "./src/main/resources/Key/"
					+ "secret_key.properties";
			File poposedSecretKeyFile = new File(proposedSecretKeyFilePath);
			currentSecretKeyFile.renameTo(poposedSecretKeyFile);
			if (!poposedSecretKeyFile.exists()) {
				System.out.println(
						"Missing secret key files at " + System.getProperty("user.dir") + "./src/main/resources/Key/");
				System.out.println("Terminating the execution, please contact the author.");
				System.exit(0);
			}

			try (InputStream input = new FileInputStream(proposedSecretKeyFilePath)) {
				decryptKeyProperties = new Properties();
				decryptKeyProperties.load(input);
			}
			poposedSecretKeyFile.renameTo(currentSecretKeyFile);
			String signatureKey = decryptKeyProperties.getProperty("Signature");

			byte[] keyBytes = Base64.getDecoder().decode(signatureKey);
			SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

			// Load the encrypted properties from the file
			decryptedProperties = new Properties();
			String keyFilePath = System.getProperty("user.dir") + "./src/main/resources/Key/" + "secretdsa";
			File keyFile = new File(keyFilePath);
			File propertiesFile = new File(
					System.getProperty("user.dir") + "./src/main/resources/Key/secret.properties");
			keyFile.renameTo(propertiesFile);
			try (InputStream input = new FileInputStream(propertiesFile)) {
				decryptedProperties.load(input);
			}
			propertiesFile.renameTo(keyFile);

			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			// Decrypt the value
			for (String propertyName : decryptedProperties.stringPropertyNames()) {
				String encryptedValue = decryptedProperties.getProperty(propertyName);
				byte[] decryptedValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
				String decryptedString = new String(decryptedValue, StandardCharsets.UTF_8);
				decryptedProperties.setProperty(propertyName, decryptedString);
			}
		} catch (Exception e) {
			throw new Exception("Invalid key, please contact the author.");
		}
		return decryptedProperties;
	}
}
