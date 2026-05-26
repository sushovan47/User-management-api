package com.demo.practice.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.demo.practice.exception.PracticeAppException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	private static final Logger logger = LogManager.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
	public void sendEmail(String to, String subject, String body) throws PracticeAppException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(to);

			helper.setSubject(subject);

			helper.setText(body, true);
			mailSender.send(message);
		} catch (Exception e) {
			logger.error("Failed to send email to {}: {}", to, e.getMessage());
			throw new PracticeAppException("Failed to send email: " + e.getMessage());
		}
	}

}
