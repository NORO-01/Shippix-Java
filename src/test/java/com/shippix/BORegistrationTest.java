package com.shippix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shippix.User_Management.DTO.BORequest;
import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Repo.BusinessOwnerRequestRepo;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import com.shippix.User_Management.Service.BusinessOwnerRequestService;
import com.shippix.User_Management.Service.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BORegistrationTest {
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

    private BORequest validRequestDto;
    private BusinessOwnerRequest savedRequest;

    @BeforeEach
    void setUp() {
        // Setup valid request DTO
        validRequestDto = new BORequest(
            "John Doe",
            "john.doe@example.com",
            "+1234567890",
            "1234567890",
            "John's Business",
            "RETAIL",
            40.7128,
            -74.0060
        );

        // Setup saved entity
        savedRequest = new BusinessOwnerRequest();
        savedRequest.setId(1L);
        savedRequest.setName("John Doe");
        savedRequest.setEmail("john.doe@example.com");
        savedRequest.setPhoneNumber("+1234567890");
        savedRequest.setNationalId("1234567890");
        savedRequest.setBusinessName("John's Business");
        savedRequest.setBusinessType("RETAIL");
        savedRequest.setLatitude(40.7128);
        savedRequest.setLongitude(-74.0060);
        savedRequest.setStatus(BusinessOwnerRequest.Status.PENDING);
    }

    @Test
    void submitRequest_WithValidData_ShouldSaveAndReturnResponseWithCorrectFields() {
        // Arrange
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(savedRequest);

        // Act
        BOResponse result = businessOwnerRequestService.submitRequest(validRequestDto);

        // Assert 
        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.id());
        assertEquals(savedRequest.getName(), result.name());
        assertEquals(savedRequest.getEmail(), result.email());
        assertEquals(savedRequest.getBusinessName(), result.businessName());
        assertEquals(savedRequest.getBusinessType(), result.businessType());
        assertEquals(BusinessOwnerRequest.Status.PENDING, result.status());

        // Verify interactions
        verify(requestRepo, times(1)).save(any(BusinessOwnerRequest.class));
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }


    @Test
    void submitRequest_ShouldSetStatusToPending() {
        // Arrange
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenAnswer(invocation -> {
            BusinessOwnerRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        // Act
        BOResponse result = businessOwnerRequestService.submitRequest(validRequestDto);

        // Assert
        assertEquals(BusinessOwnerRequest.Status.PENDING, result.status());
    }

    @Test
    void submitRequest_WithNullDto_ShouldThrowException() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> {
            businessOwnerRequestService.submitRequest(null);
        });

        // Verify no interactions with repositories
        verifyNoInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    // T1: Business Name – Must not be blank.
    @Test
    void submitRequest_WithBlankBusinessName_ShouldThrowException_T1() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Business Name must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T2: Business Name – Special characters are not allowed.
    @Test
    void submitRequest_WithSpecialCharsInBusinessName_ShouldThrowException_T2() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business@Name#", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Special characters are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T3: Business Name – First character cannot have space.
    @Test
    void submitRequest_WithSpaceFirstCharInBusinessName_ShouldThrowException_T3() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            " Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("First character cannot have space"));
        verifyNoInteractions(requestRepo);
    }

    // T4: Owner Name – Must not be blank.
    @Test
    void submitRequest_WithBlankOwnerName_ShouldThrowException_T4() {
        BORequest invalidRequest = new BORequest(
            "", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Owner Name must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T5: Owner Name – Numbers are not allowed.
    @Test
    void submitRequest_WithNumbersInOwnerName_ShouldThrowException_T5() {
        BORequest invalidRequest = new BORequest(
            "John123 Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Numbers are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T6: Owner Name – Special characters are not allowed.
    @Test
    void submitRequest_WithSpecialCharsInOwnerName_ShouldThrowException_T6() {
        BORequest invalidRequest = new BORequest(
            "John@Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Special characters are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T7: Owner Name – First character cannot have space.
    @Test
    void submitRequest_WithSpaceFirstCharInOwnerName_ShouldThrowException_T7() {
        BORequest invalidRequest = new BORequest(
            " John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("First character cannot have space"));
        verifyNoInteractions(requestRepo);
    }

    // T8: Email – Must not be blank.
    @Test
    void submitRequest_WithBlankEmail_ShouldThrowException_T8() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Email must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T9: Email – Must be in valid format.
    @Test
    void submitRequest_WithInvalidEmailFormat_ShouldThrowException_T9() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "invalid-email", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Email must be in valid format"));
        verifyNoInteractions(requestRepo);
    }

    // T10: Email – First character cannot have space.
    @Test
    void submitRequest_WithSpaceFirstCharInEmail_ShouldThrowException_T10() {
        BORequest invalidRequest = new BORequest(
            "John Doe", " john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("First character cannot have space"));
        verifyNoInteractions(requestRepo);
    }

    // T11: Phone Number – Must not be blank.
    @Test
    void submitRequest_WithBlankPhoneNumber_ShouldThrowException_T11() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Phone Number must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T12: Phone Number – Special characters are not allowed.
    @Test
    void submitRequest_WithInvalidSpecialCharsInPhone_ShouldThrowException_T12() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+20@123#456", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Special characters are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T13: Phone Number – Characters are not allowed.
    @Test
    void submitRequest_WithLettersInPhoneNumber_ShouldThrowException_T13() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+20ABC123456", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Characters are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T14: Phone Number – First character cannot have space.
    @Test
    void submitRequest_WithSpaceFirstCharInPhone_ShouldThrowException_T14() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", " +201234567890", "12345678901234", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("First character cannot have space"));
        verifyNoInteractions(requestRepo);
    }

    // T24: National ID – Must not be blank.
    @Test
    void submitRequest_WithBlankNationalId_ShouldThrowException_T24() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("National ID must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T25: National ID – Must be exactly 14 digits.
    @Test
    void submitRequest_WithInvalidLengthNationalId_ShouldThrowException_T25() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "1234567890", // 10 digits
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("National ID must be exactly 14 digits"));
        verifyNoInteractions(requestRepo);
    }

    // T26: National ID – Special characters are not allowed.
    @Test
    void submitRequest_WithSpecialCharsInNationalId_ShouldThrowException_T26() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "1234567890ABCD", 
            "Business Name", "RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Special characters are not allowed"));
        verifyNoInteractions(requestRepo);
    }

    // T27: Business Type – Must not be blank.
    @Test
    void submitRequest_WithBlankBusinessType_ShouldThrowException_T27() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Business Type must not be blank"));
        verifyNoInteractions(requestRepo);
    }

    // T28: Business Type – Must be selected from a predefined list.
    @Test
    void submitRequest_WithInvalidBusinessType_ShouldThrowException_T28() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", "INVALID_TYPE", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Business Type must be selected from predefined list"));
        verifyNoInteractions(requestRepo);
    }

    // T29: Business Type – First character cannot have space.
    @Test
    void submitRequest_WithSpaceFirstCharInBusinessType_ShouldThrowException_T29() {
        BORequest invalidRequest = new BORequest(
            "John Doe", "john@example.com", "+201234567890", "12345678901234", 
            "Business Name", " RETAIL", 40.7128, -74.0060
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessOwnerRequestService.submitRequest(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("First character cannot have space"));
        verifyNoInteractions(requestRepo);
    }

    // Valid request test
    @Test
    void submitRequest_WithValidData_ShouldSaveAndReturnResponse() {
        when(requestRepo.save(any(BusinessOwnerRequest.class))).thenReturn(savedRequest);

        BOResponse result = businessOwnerRequestService.submitRequest(validRequestDto);

        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.id());
        assertEquals(savedRequest.getName(), result.name());
        assertEquals(savedRequest.getEmail(), result.email());
        assertEquals(savedRequest.getBusinessName(), result.businessName());
        assertEquals(savedRequest.getBusinessType(), result.businessType());
        assertEquals(BusinessOwnerRequest.Status.PENDING, result.status());

        verify(requestRepo, times(1)).save(any(BusinessOwnerRequest.class));
        verifyNoMoreInteractions(requestRepo, userRepo, passwordTokenRepo, emailService, passwordEncoder);
    }

    // Test that valid business types are accepted
    @Test
    void submitRequest_WithAllValidBusinessTypes_ShouldSaveSuccessfully() {
        String[] validTypes = {"RETAIL", "WHOLESALE", "MANUFACTURING", "SERVICES", "RESTAURANT", "HOSPITALITY"};
        
        for (String businessType : validTypes) {
            BORequest validRequest = new BORequest(
                "John Doe", "john@example.com", "+201234567890", "12345678901234", 
                "Business Name", businessType, 40.7128, -74.0060
            );

            when(requestRepo.save(any(BusinessOwnerRequest.class))).thenAnswer(invocation -> {
                BusinessOwnerRequest request = invocation.getArgument(0);
                request.setId(1L);
                return request;
            });

            assertDoesNotThrow(() -> {
                businessOwnerRequestService.submitRequest(validRequest);
            });

            verify(requestRepo, times(1)).save(any(BusinessOwnerRequest.class));
            reset(requestRepo); // Reset mock for next iteration
        }
    }


}
