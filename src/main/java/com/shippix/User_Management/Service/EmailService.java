package com.shippix.User_Management.Service;

import com.shippix.User_Management.Email.EmailTemplate;
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

    public void sendEmail(EmailTemplate template) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(from);
            helper.setTo(template.getTo());
            helper.setSubject(template.getSubject());

            String htmlContent = "<p>" + template.getBody() + "</p>" +
                    (template.getLink() != null ? "<p>Click <a href='" + template.getLink() + "'>here</a> to access your page.</p>" : "");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            System.out.println("Email sent successfully to: " + template.getTo());

        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + template.getTo() + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error while sending email to " + template.getTo() + ": " + e.getMessage());
        }
    }
}