package com.shippix;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
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
        pendingRequest.setPhoneNumber("201234567890");
        pendingRequest.setNationalId("12345678901234");
        pendingRequest.setBusinessName("Maya's Business");
        pendingRequest.setBusinessType(com.shippix.User_Management.Model.BusinessType.RETAIL_STORE);
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
        assertTrue(pendingRequest.getStatus() == BusinessOwnerRequest.Status.APPROVED);

        // Verify interactions
        verify(requestRepo, times(1)).findById(1L);
        verify(requestRepo, times(1)).save(pendingRequest);
        verify(userRepo, times(1)).save(any(BusinessOwner.class));
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

        // Assert 
        assertTrue(pendingRequest.getStatus() == BusinessOwnerRequest.Status.REJECTED);
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

    @AfterEach
    void tearDown() {
        // Reset all mocks to clear any interactions
        reset(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }
}