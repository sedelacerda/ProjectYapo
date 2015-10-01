import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {

	private final static String ADMIN_EMAIL = "projectyapo@gmail.com";
	private final static String ADMIN_PASS = "giulietta2015";

	public static void sendSimpleEmail(String subject, String message, String[] to) {

		// Set properties
		String host = "smtp.gmail.com";
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", ADMIN_EMAIL);
		props.put("mail.smtp.password", ADMIN_PASS);
		props.put("mail.smtp.port", 587);
		props.put("mail.smtp.auth", true);

		Session session = Session.getDefaultInstance(props, null);

		MimeMessage mimeMessage = new MimeMessage(session);

		try {

			mimeMessage.setFrom(new InternetAddress(ADMIN_EMAIL));
			InternetAddress[] toAddress = new InternetAddress[to.length];

			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				mimeMessage.addRecipient(RecipientType.TO, toAddress[i]);
			}

			// Set subject
			mimeMessage.setSubject(subject);

			// Set message content
			mimeMessage.setText(message);

			// Send
			if (1 < toAddress.length)
				MainWindow.insertNewProgramCurrentState(
						"Enviando email con asunto \"" + subject + "\" a " + toAddress.length + " usuarios.",
						Color.BLACK);

			else
				MainWindow.insertNewProgramCurrentState(
						"Enviando email con asunto \"" + subject + "\" a " + toAddress[0] + ".", Color.BLACK);

			Transport transport = session.getTransport("smtp");
			transport.connect(host, ADMIN_EMAIL, ADMIN_PASS);
			transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
			transport.close();

		} catch (MessagingException e) {
			MainWindow.insertNewProgramCurrentState("Error al enviar email.", Color.BLACK);
			e.printStackTrace();
		}
	}

	public static void sendFileEmail(String subject, String message, String[] attachFiles, String[] to) {

		// sets SMTP server properties
		String host = "smtp.gmail.com";
		Properties properties = new Properties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", 587);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.user", ADMIN_EMAIL);
		properties.put("mail.password", ADMIN_PASS);

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASS);
			}
		};
		Session session = Session.getInstance(properties, auth);

		// creates a new e-mail message
		Message msg = new MimeMessage(session);

		try {
			msg.setFrom(new InternetAddress(ADMIN_EMAIL));

			InternetAddress[] toAddresses = new InternetAddress[to.length];

			for (int i = 0; i < to.length; i++) {
				toAddresses[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddresses.length; i++) {
				msg.addRecipient(RecipientType.TO, toAddresses[i]);
			}
			msg.setSubject(subject);
			msg.setSentDate(new Date());

			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(message, "text/html");

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// adds attachments
			if (attachFiles != null && attachFiles.length > 0) {
				for (String filePath : attachFiles) {
					MimeBodyPart attachPart = new MimeBodyPart();
					attachPart.attachFile(filePath);
					multipart.addBodyPart(attachPart);
				}
			}

			// sets the multi-part as e-mail's content
			msg.setContent(multipart);

			if (1 < to.length)
				MainWindow.insertNewProgramCurrentState(
						"Enviando email con asunto \"" + subject + "\" a " + to.length + " usuarios.", Color.BLACK);

			else
				MainWindow.insertNewProgramCurrentState(
						"Enviando email con asunto \"" + subject + "\" a " + to[0] + ".", Color.BLACK);

			// sends the e-mail
			Transport.send(msg);

		} catch (AddressException e) {
			MainWindow.insertNewProgramCurrentState("Error al enviar email.", Color.BLACK);
			e.printStackTrace();
		} catch (MessagingException e) {
			MainWindow.insertNewProgramCurrentState("Error al enviar email.", Color.BLACK);
			e.printStackTrace();
		} catch (IOException ex) {
			MainWindow.insertNewProgramCurrentState("Error al enviar email.", Color.BLACK);
			ex.printStackTrace();
		}
	}

}
