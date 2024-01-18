package com.joq.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;

import com.joq.exception.AutomationException;
import com.joq.keywords.DataHandler;

public class Utilities {

	public Random random;

	/**
	 * To send report as attachment in mail
	 * 
	 * @author sanoj.swaminathan
	 * @since 29-May-2018
	 * 
	 */
	public static void sendMail() {
		try {
			String message = "", recipientMaillists = "";

			final String yourMail = new DataHandler()
					.getProperty(AutomationConstants.EMAIL_CONFIG, AutomationConstants.YOUR_MAIL).trim();
			final String yourMailPassword = new DataHandler()
					.getProperty(AutomationConstants.EMAIL_CONFIG, AutomationConstants.YOUR_MAIL_PASSWORD).trim();
			recipientMaillists = new DataHandler()
					.getProperty(AutomationConstants.EMAIL_CONFIG, AutomationConstants.RECIPIENT_MAIL_LISTS).trim();

			Properties properties = new Properties();
			properties.put("mail.smtp.host", "smtp.gmail.com");
			properties.put("mail.smtp.port", "587");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");

			properties.put("mail.user", yourMail);
			properties.put("mail.password", yourMailPassword);

			Authenticator auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(yourMail, yourMailPassword);
				}
			};
			Session session = Session.getInstance(properties, auth);

			DateFormat dff = new SimpleDateFormat("EEE MMM dd, yyyy HH:mm:ss z");

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(yourMail));
			msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientMaillists));
			msg.setSubject("Performance Test Execution Report" + " - " + dff.format(new Date()).toString());
			msg.setSentDate(new Date());

			try {
				message = "Hi, <p> Please find the attached report to know more details";
			} catch (final Exception e) {
				e.printStackTrace();
			}

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(message, "text/html");

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// compress the execution reports

			compress(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results");

			// adds attachments
			List<String> filesList = new ArrayList<String>();
			File[] files = new File(System.getProperty("user.dir") + "//Email_Execution_Reports//").listFiles();
			for (File file : files) {
				if (file.isFile()) {
					filesList
							.add(System.getProperty("user.dir") + "//Email_Execution_Reports//" + "/" + file.getName());
				}
			}

			if (filesList != null && filesList.size() > 0) {
				for (String filePath : filesList) {
					MimeBodyPart attachPart = new MimeBodyPart();
					try {
						attachPart.attachFile(filePath);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					multipart.addBodyPart(attachPart);
				}
			}

			// sets the multi-part as e-mail's content
			msg.setContent(multipart);

			// sends the e-mail
			Transport.send(msg);

			// Delete Emal_Execution_Reports temporary folder
			FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "//Email_Execution_Reports//"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To compress the directory for report
	 * 
	 * @author sanoj.swaminathan
	 * @since 29-May-2018
	 * @param dirPath
	 */
	public static void compress(String dirPath) {
		final Path sourceDir = Paths.get(dirPath);
		String zipFileName = dirPath.concat("_" + getCurrentDateAndTime() + ".zip");
		try {
			final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
			Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					try {
						Path targetFile = sourceDir.relativize(file);
						outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
						byte[] bytes = Files.readAllBytes(file);
						outputStream.write(bytes, 0, bytes.length);
						outputStream.closeEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to get a random number between the two ranges
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param lowerBound
	 * @param upperBound
	 * @throws AutomationException
	 */
	public int getRandomNumber(int lowerBound, int upperBound) throws AutomationException {
		int randomNum = 0;
		try {
			random = new Random();
			randomNum = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomNum;
	}

	/**
	 * Method to get a random number with the a number length mentioned
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param numberLength
	 * @throws AutomationException
	 */
	public String getRandomNumber(int numberLength) throws AutomationException {
		String randomNumber = null;
		try {
			random = new Random();
			int randomNum = 0;
			boolean loop = true;
			while (loop) {
				randomNum = random.nextInt();
				if (Integer.toString(randomNum).length() == numberLength
						&& !Integer.toString(randomNum).startsWith("-")) {
					loop = false;
				}
			}
			randomNumber = Integer.toString(randomNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomNumber;
	}

	/**
	 * Method to get a random string value with the string length mentioned
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param stringLength
	 * @throws AutomationException
	 */
	public String getRandomString(int stringLength) throws AutomationException {
		try {
			String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
			StringBuilder sb = new StringBuilder(stringLength);
			for (int i = 0; i < stringLength; i++) {
				int index = (int) (AlphaNumericString.length() * Math.random());
				sb.append(AlphaNumericString.charAt(index));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to get a random string which has only alphabets with the string length
	 * mentioned
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param stringLength
	 * @throws AutomationException
	 */
	public String getRandomStringOnlyAlphabets(int stringLength) throws AutomationException {
		try {
			String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvxyz";
			StringBuilder sb = new StringBuilder(stringLength);
			for (int i = 0; i < stringLength; i++) {
				int index = (int) (AlphaNumericString.length() * Math.random());
				sb.append(AlphaNumericString.charAt(index));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to get the current date
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @throws AutomationException
	 */
	public String getCurrentDate() throws AutomationException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
			Date date = new Date();
			String filePathdate = dateFormat.format(date).toString();
			return filePathdate;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to get the current date in the date format ddMMMyyyy
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @throws AutomationException
	 */
	public String getCurrentDateInFormatddMMMyyyy() throws AutomationException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
			Date date = new Date();
			String filePathdate = dateFormat.format(date).toString();
			return filePathdate;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to get a current date in the date format ddMMyyyy
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @throws AutomationException
	 */
	public String getCurrentDateInFormatddMMyyyy() throws AutomationException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date date = new Date();
			String filePathdate = dateFormat.format(date).toString();
			return filePathdate;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to get the day from the current date
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @throws AutomationException
	 */
	public String getDayFromCurrentDate() throws AutomationException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
			Date date = new Date();
			String filePathdate = dateFormat.format(date).toString();
			String day = filePathdate.substring(0, 2);
			return day;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a double value to an Integer
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param doubleValue
	 * @throws AutomationException
	 */
	public int convertDoubleToInt(double doubleValue) throws AutomationException {
		try {
			int intValue = (int) doubleValue;
			return intValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a float value to an Integer
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param floatValue
	 * @throws AutomationException
	 */
	public int convertFloatToInt(float floatValue) throws AutomationException {
		try {
			int intValue = (int) floatValue;
			return intValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a string value to an Integer
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param stringValue
	 * @throws AutomationException
	 */
	public int convertStringToInt(String stringValue) throws AutomationException {
		try {
			int intValue = Integer.parseInt(stringValue);
			return intValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a string value to a double value
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param stringValue
	 * @throws AutomationException
	 */
	public double convertStringToDouble(String stringValue) throws AutomationException {
		try {
			double doubleValue = Double.parseDouble(stringValue);
			return doubleValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert an Integer to a string value
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param intValue
	 * @throws AutomationException
	 */
	public String convertIntToString(int intValue) throws AutomationException {
		try {
			String stringValue = String.valueOf(intValue);
			return stringValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a double value to a string value
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param doubleValue
	 * @throws AutomationException
	 */
	public String convertDoubleToString(double doubleValue) throws AutomationException {
		try {
			String stringValue = String.valueOf(doubleValue);
			return stringValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to convert a string value to a long value
	 * 
	 * @author sanoj.swaminathan
	 * @since 20-04-2020
	 * @modified 31-03-2023
	 * @param doubleValue
	 * @throws AutomationException
	 */
	public long convertStringToLong(String stringValue) throws AutomationException {
		try {
			long longValue = Long.parseLong(stringValue);
			return longValue;
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
	}

	/**
	 * Method to encode any file data
	 * 
	 * @author sanoj.swaminathan
	 * @since 06-03-2023
	 * @modified 31-03-2023
	 * @param filePath
	 * @throws AutomationException
	 */
	public String encodeFile(String filePath) throws AutomationException {
		String encodedString = null;
		try {
			byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
			encodedString = Base64.getEncoder().encodeToString(fileContent);
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
		return encodedString;
	}

	/**
	 * Method to encode strings
	 * 
	 * @author sanoj.swaminathan
	 * @since 13-03-2021
	 * @param stringToEncode
	 * @return
	 * @throws AutomationException
	 */
	public String encodeStrings(final String stringToEncode) throws AutomationException {
		byte[] encoded;
		try {
			encoded = Base64.getEncoder().encode(stringToEncode.getBytes());
		} catch (final Exception lException) {
			throw new AutomationException(getExceptionMessage(), lException);
		}
		return new String(encoded);
	}

	/**
	 * Method to decode any string data
	 * 
	 * @author sanoj.swaminathan
	 * @since 06-03-2023
	 * @modified 31-03-2023
	 * @param dataToBeDecoded
	 * @throws AutomationException
	 */
	public String decodeStrings(String dataToBeDecoded) throws AutomationException {
		byte[] decodedString = null;
		try {
			decodedString = Base64.getDecoder().decode(dataToBeDecoded);
		} catch (Exception e) {
			throw new AutomationException(getExceptionMessage() + "\n" + AutomationConstants.CAUSE + e.getMessage());
		}
		return decodedString.toString();
	}

	/**
	 * Method to delete file
	 * 
	 * @author sanoj.swaminathan
	 * @since 21-03-2023
	 * @modified 31-03-2023
	 * @param filePath
	 */
	public void deleteFile(String filePath) {
		try {
			FileUtils.forceDelete(new File(filePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to get the Exception message, to pass the message whenever an
	 * exception is encountered
	 * 
	 * @author sanoj.swaminathan
	 * @since 13-04-2020
	 * @modified 31-03-2023
	 */
	public String getExceptionMessage() {
		StringBuffer message = new StringBuffer();
		try {
			message.append("Exception in ");
			message.append(Thread.currentThread().getStackTrace()[2].getClassName());
			message.append(".");
			message.append(Thread.currentThread().getStackTrace()[2].getMethodName());
			message.append("()");
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return message.toString();
	}

	/**
	 * To get the current date and time
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-May-2018
	 * @return
	 */
	private static String getCurrentDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
		Date date = new Date();
		String currdate = dateFormat.format(date);
		String currtime = timeFormat.format(date);
		return currdate + "_" + currtime;
	}
}
