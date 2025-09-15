package com.shippix;

import com.shippix.User_Management.Email.EmailTemplate;
import com.shippix.User_Management.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailTemplate emailTemplate;

    @Mock
    private MimeMessage mimeMessage;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    private String fromEmail = "sender@example.com";

    @BeforeEach
    void setUp() throws Exception {
        // Set up the EmailTemplate mock with valid defaults
        when(emailTemplate.getTo()).thenReturn("test@example.com");
        when(emailTemplate.getSubject()).thenReturn("Test Subject");
        when(emailTemplate.getBody()).thenReturn("Test Body");
        when(emailTemplate.getLink()).thenReturn("https://example.com");

        // Mock the MimeMessage creation
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use reflection to set the 'from' field in EmailService
        java.lang.reflect.Field fromField = EmailService.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(emailService, fromEmail);
    }

    @Test
    void testSendEmail_Success() throws Exception {
        // Act
        emailService.sendEmail(emailTemplate);

        // Assert
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        assertEquals(mimeMessage, mimeMessageCaptor.getValue());
    }

    @Test
    void testSendEmail_WithoutLink() throws Exception {
        // Arrange
        when(emailTemplate.getLink()).thenReturn(null);

        // Act
        emailService.sendEmail(emailTemplate);

        // Assert
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        assertEquals(mimeMessage, mimeMessageCaptor.getValue());
    }

    @Test
    void testSendEmail_MessagingExceptionDuringSetup() throws Exception {
        // Arrange - Simulate exception during MimeMessageHelper setup
        doThrow(new MessagingException("Failed to set content"))
            .when(mimeMessage).setContent(any(), anyString());

        // Act
        emailService.sendEmail(emailTemplate);

        // Assert
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_GeneralException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(javaMailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendEmail(emailTemplate);

        // Assert
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }


}