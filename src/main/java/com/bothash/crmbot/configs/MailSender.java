package com.bothash.crmbot.configs;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@PropertySource("classpath:application.properties")
@Service
public class MailSender {

	protected static Logger logger = Logger.getLogger(MailSender.class.getName());

	@Value("${mailServerHost}")
	private String mailServerHost;

	@Value("${mailServerPort}")
	private int mailServerPort;

	@Value("${mailServerUsername}")
	private String mailServerUsername;

	@Value("${mailServerPassword}")
	private String mailServerPassword;

	@Value("${mailServerUseSSL}")
	private boolean mailServerUseSSL;

	@Value("${mailServerUseTLS}")
	private boolean mailServerUseTLS;

	@Value("${mailServerDefaultFrom}")
	private String mailServerDefaultFrom;

	

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		MailSender.logger = logger;
	}

	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public int getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(int mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	public String getMailServerUsername() {
		return mailServerUsername;
	}

	public void setMailServerUsername(String mailServerUsername) {
		this.mailServerUsername = mailServerUsername;
	}

	public String getMailServerPassword() {
		return mailServerPassword;
	}

	public void setMailServerPassword(String mailServerPassword) {
		this.mailServerPassword = mailServerPassword;
	}

	public boolean isMailServerUseSSL() {
		return mailServerUseSSL;
	}

	public void setMailServerUseSSL(boolean mailServerUseSSL) {
		this.mailServerUseSSL = mailServerUseSSL;
	}

	public boolean isMailServerUseTLS() {
		return mailServerUseTLS;
	}

	public void setMailServerUseTLS(boolean mailServerUseTLS) {
		this.mailServerUseTLS = mailServerUseTLS;
	}

	public String getMailServerDefaultFrom() {
		return mailServerDefaultFrom;
	}

	public void setMailServerDefaultFrom(String mailServerDefaultFrom) {
		this.mailServerDefaultFrom = mailServerDefaultFrom;
	}

	
	public void sendMail(Map<String, String> variables) {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", mailServerPort);
		props.put("mail.smtp.ssl.trust","smtp.gmail.com");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailServerUsername, mailServerPassword);
			}
		});

		try {

			MimeMessage message = new MimeMessage(session);
			// Set From Field: adding senders email to from field.
			message.setFrom(new InternetAddress(mailServerUsername));
			// Set To Field: adding recipient's email to from field.
			message.setRecipients(Message.RecipientType.TO, variables.get("emailTo"));
			// Set Subject: subject of the email
			message.setSubject(variables.get("emailSubject"));
			// set body of the email.
			message.setContent(variables.get("emailBody"), "text/html");

			Transport.send(message);

			logger.info("Email Sent successfully.");

		} catch (MessagingException e) {
			e.printStackTrace();
			logger.info("Unable to send mail MessagingException:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Unable to send mail MessagingException:" + e.getMessage());
		}
	}
}
