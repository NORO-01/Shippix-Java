package com.shippix;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.DTO.EmailBody;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.BusinessOwnerRequestRepo;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import com.shippix.User_Management.Service.BusinessOwnerRequestService;
import com.shippix.User_Management.Service.EmailService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private BusinessOwnerRequestRepo requestRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordTokenRepo passwordTokenRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private BusinessOwnerRequestService businessOwnerRequestService;

    private BusinessOwnerRequest pendingRequest;
    private BusinessOwnerRequest approvedRequest;
    private BusinessOwnerRequest rejectedRequest;

    @BeforeEach
    void setUp() {
        // Setup pending request
        pendingRequest = new BusinessOwnerRequest();
        pendingRequest.setId(1L);
        pendingRequest.setName("Maya Fouad");
        pendingRequest.setEmail("mayafouad2004@gmail.com");
        pendingRequest.setPhoneNumber("+201234567890");
        pendingRequest.setNationalId("12345678901234");
        pendingRequest.setBusinessName("Maya's Business");
        pendingRequest.setBusinessType("RETAIL");
        pendingRequest.setLatitude(40.7128);
        pendingRequest.setLongitude(-74.0060);
        pendingRequest.setStatus(BusinessOwnerRequest.Status.PENDING);

        // Setup approved request
        approvedRequest = new BusinessOwnerRequest();
        approvedRequest.setId(1L);
        approvedRequest.setStatus(BusinessOwnerRequest.Status.APPROVED);

        // Setup rejected request
        rejectedRequest = new BusinessOwnerRequest();
        rejectedRequest.setId(1L);
        rejectedRequest.setStatus(BusinessOwnerRequest.Status.REJECTED);
    }

    // approveRequest tests ----------------------------------------------------------------------------

    @Test
    void approveRequest_WithValidPendingRequest_ShouldApproveAndCreateBusinessOwner() throws Exception {
        // Arrange
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(approvedRequest);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(BusinessOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordTokenRepo.save(any(PasswordToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmail(any(EmailBody.class));

        // Act
        BusinessOwner result = businessOwnerRequestService.approveRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(pendingRequest.getEmail(), result.getEmail());
        assertEquals(pendingRequest.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(pendingRequest.getBusinessName(), result.getBusinessName());
        assertEquals(pendingRequest.getBusinessType(), result.getBusinessType());
        assertEquals(pendingRequest.getNationalId(), result.getNationalId());
        assertEquals(pendingRequest.getLatitude(), result.getLatitude());
        assertEquals(pendingRequest.getLongitude(), result.getLongitude());
        assertEquals(Users.Role.ROLE_BUSINESS_OWNER, result.getRole());
        assertEquals("encodedPassword", result.getPassword());

        // Verify interactions
        verify(requestRepo, times(1)).findById(1L);
        verify(requestRepo, times(1)).save(pendingRequest);
        verify(userRepo, times(1)).save(any(BusinessOwner.class));
        verify(passwordTokenRepo, times(1)).save(any(PasswordToken.class));
        verify(emailService, times(1)).sendEmail(any(EmailBody.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void approveRequest_WithNonExistentRequest_ShouldThrowException() {
        // Arrange
        when(requestRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            businessOwnerRequestService.approveRequest(999L);
        });

        assertEquals("Request not found", exception.getMessage());
        verify(requestRepo, times(1)).findById(999L);
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    @Test
    void approveRequest_WithAlreadyApprovedRequest_ShouldThrowException() {
        // Arrange
        pendingRequest.setStatus(BusinessOwnerRequest.Status.APPROVED);
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            businessOwnerRequestService.approveRequest(1L);
        });

        assertEquals("Request already processed", exception.getMessage());
        verify(requestRepo, times(1)).findById(1L);
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    @Test
    void approveRequest_WithAlreadyRejectedRequest_ShouldThrowException() {
        // Arrange
        pendingRequest.setStatus(BusinessOwnerRequest.Status.REJECTED);
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            businessOwnerRequestService.approveRequest(1L);
        });

        assertEquals("Request already processed", exception.getMessage());
        verify(requestRepo, times(1)).findById(1L);
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    
    @Test
    void approveRequest_ShouldCreatePasswordTokenWithCorrectExpiry() throws Exception{
        // Arrange
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(approvedRequest);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(BusinessOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<PasswordToken> tokenCaptor = ArgumentCaptor.forClass(PasswordToken.class);
        when(passwordTokenRepo.save(tokenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(emailService).sendEmail(any(EmailBody.class));

        long currentTime = System.currentTimeMillis();

        // Act
        businessOwnerRequestService.approveRequest(1L);

        // Assert
        PasswordToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken);
        assertNotNull(savedToken.getToken());
        assertTrue(savedToken.getToken().length() > 0);
        assertNotNull(savedToken.getUser());
        assertNotNull(savedToken.getExpiryTime());
        
        // Check that expiry time is approximately 3 days from now
        long expectedExpiry = currentTime + (1000L * 60 * 60 * 24 * 3);
        long actualExpiry = savedToken.getExpiryTime().getTime();
        assertTrue(Math.abs(expectedExpiry - actualExpiry) < 1000); // Allow 1 second difference
    }

   @Test
    void approveRequest_ShouldSendEmailWithCorrectContent() throws Exception { 
        // Arrange
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(approvedRequest);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(BusinessOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<PasswordToken> tokenCaptor = ArgumentCaptor.forClass(PasswordToken.class);
        when(passwordTokenRepo.save(tokenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<EmailBody> emailCaptor = ArgumentCaptor.forClass(EmailBody.class);
        doNothing().when(emailService).sendEmail(emailCaptor.capture()); // now compiles

        // Act
        businessOwnerRequestService.approveRequest(1L);

        // Assert
        PasswordToken savedToken = tokenCaptor.getValue();
        EmailBody sentEmail = emailCaptor.getValue();

        assertNotNull(sentEmail);
        assertEquals(pendingRequest.getEmail(), sentEmail.to());
        assertEquals("Set up your Shippix account password", sentEmail.subject());
        assertTrue(sentEmail.text().contains("Welcome to Shippix!"));
        assertNotNull(sentEmail.link());
        assertTrue(sentEmail.link().contains("https://localhost:8080/set-password?token=" + savedToken.getToken()));
    }


    // rejectRequest tests ----------------------------------------------------------------------------

    @Test
    void rejectRequest_WithNonExistentRequest_ShouldThrowException() {
        // Arrange
        when(requestRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            businessOwnerRequestService.rejectRequest(999L);
        });

        assertEquals("Request not found", exception.getMessage());
        verify(requestRepo, times(1)).findById(999L);
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    @Test
    void rejectRequest_ShouldSetStatusToRejected() {
        // Arrange
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenAnswer(invocation -> {
            BusinessOwnerRequest request = invocation.getArgument(0);
            assertEquals(BusinessOwnerRequest.Status.REJECTED, request.getStatus());
            return request;
        });

        // Act
        businessOwnerRequestService.rejectRequest(1L);

        // Assert - Status is verified in the mock above
        verify(requestRepo, times(1)).save(pendingRequest);
    }

    @Test
    void rejectRequest_ShouldNotCreateUserOrSendEmail() {
        // Arrange
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(rejectedRequest);

        // Act
        businessOwnerRequestService.rejectRequest(1L);

        // Assert - No user creation or email sending should occur
        verify(requestRepo, times(1)).findById(1L);
        verify(requestRepo, times(1)).save(pendingRequest);
        verifyNoInteractions(userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    @Test
    void rejectRequest_WithAlreadyProcessedRequest_ShouldStillReject() {
        // Arrange - Even if already approved/rejected, we should still be able to set to rejected
        pendingRequest.setStatus(BusinessOwnerRequest.Status.APPROVED);
        when(requestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(rejectedRequest);

        // Act
        BOResponse result = businessOwnerRequestService.rejectRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(BusinessOwnerRequest.Status.REJECTED, result.status());
        verify(requestRepo, times(1)).save(pendingRequest);
    }

    @AfterEach
    void tearDown() {
        // Reset all mocks to clear any interactions
        reset(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }
}