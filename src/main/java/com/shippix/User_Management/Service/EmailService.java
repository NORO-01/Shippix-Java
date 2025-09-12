package com.shippix.User_Management.Service;

import com.shippix.User_Management.DTO.EmailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(EmailBody emailBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(from);
        helper.setTo(emailBody.to());
        helper.setSubject(emailBody.subject());

        String htmlContent = "<p>" + emailBody.text() + "</p>" +
                (emailBody.link() != null ? "<p>Click <a href='" + emailBody.link() + "'>here</a> to access your page.</p>" : "");
        helper.setText(htmlContent, true); // true = HTML

        javaMailSender.send(message);
    }
}
