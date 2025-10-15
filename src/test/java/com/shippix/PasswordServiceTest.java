package com.shippix;

import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import com.shippix.User_Management.Service.ForgetPassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordTokenRepo tokenRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ForgetPassService forgetPassService;

    private Users testUser;
    private PasswordToken testToken;

    @BeforeEach
    void setUp() {

        testUser = new Users();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("oldPassword");

        testToken = PasswordToken.builder()
                .id(1L)
                .otp(123456)
                .expiryTime(new Date(System.currentTimeMillis() + 70000)) // 70 seconds in future
                .user(testUser)
                .verified(false)
                .build();
    }

    @Test
    void testCreateOtp_UserFound_Success() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.save(any(PasswordToken.class))).thenAnswer(invocation -> {
            PasswordToken token = invocation.getArgument(0);
            return token;
        });

        // Act
        PasswordToken result = forgetPassService.createOtp("test@example.com");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getOtp());
        assertTrue(result.getOtp() >= 100000 && result.getOtp() <= 999999);
        assertNotNull(result.getExpiryTime());
        assertEquals(testUser, result.getUser());

        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).deleteByUser(testUser);
        verify(tokenRepo, times(1)).save(any(PasswordToken.class));
    }

    @Test
    void testCreateOtp_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            forgetPassService.createOtp("nonexistent@example.com");
        });

        verify(userRepo, times(1)).findByEmail("nonexistent@example.com");
        verify(tokenRepo, never()).deleteByUser(any());
        verify(tokenRepo, never()).save(any());
    }

    @Test
    void testVerifyOtp_ValidOtp_Success() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByOtpAndUser(123456, testUser)).thenReturn(Optional.of(testToken));

        // Act
        assertDoesNotThrow(() -> forgetPassService.verifyOtp("test@example.com", 123456));

        // Assert
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).findByOtpAndUser(123456, testUser);
        verify(tokenRepo, times(1)).save(testToken);
        assertTrue(testToken.isVerified());
    }

    @Test
    void testVerifyOtp_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            forgetPassService.verifyOtp("nonexistent@example.com", 123456);
        });

        verify(userRepo, times(1)).findByEmail("nonexistent@example.com");
        verify(tokenRepo, never()).findByOtpAndUser(anyInt(), any());
    }

    @Test
    void testVerifyOtp_InvalidOtp_ThrowsException() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByOtpAndUser(999999, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            forgetPassService.verifyOtp("test@example.com", 999999);
        });

        assertEquals("Invalid OTP", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).findByOtpAndUser(999999, testUser);
    }

    @Test
    void testVerifyOtp_ExpiredOtp_ThrowsException() {
        // Arrange
        PasswordToken expiredToken = PasswordToken.builder()
                .id(1L)
                .otp(123456)
                .expiryTime(new Date(System.currentTimeMillis() - 1000)) // 1 second in past
                .user(testUser)
                .build();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByOtpAndUser(123456, testUser)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            forgetPassService.verifyOtp("test@example.com", 123456);
        });

        assertEquals("OTP expired", exception.getMessage());
        verify(tokenRepo, times(1)).deleteById(1L);
        verify(tokenRepo, never()).save(any());
    }

    @Test
    void testChangePassword_ValidOtp_Success() {
        // Arrange
        testToken.setVerified(true);
        String newPassword = "newSecurePassword123";
        String encodedPassword = "encodedNewPassword";

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByUser(testUser)).thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepo.save(testUser)).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> forgetPassService.changePassword("test@example.com", newPassword));

        // Assert
        assertEquals(encodedPassword, testUser.getPassword());
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).findByUser(testUser);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepo, times(1)).save(testUser);
        verify(tokenRepo, times(1)).delete(testToken);
    }

    @Test
    void testChangePassword_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            forgetPassService.changePassword("nonexistent@example.com", "newPassword");
        });

        verify(userRepo, times(1)).findByEmail("nonexistent@example.com");
        verify(tokenRepo, never()).findByUser(any());
    }

    @Test
    void testChangePassword_NoOtpFound_ThrowsException() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByUser(testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            forgetPassService.changePassword("test@example.com", "newPassword");
        });

        assertEquals("No OTP found", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testChangePassword_OtpNotVerified_ThrowsException() {
        // Arrange
        testToken.setVerified(false); // Explicitly set to false
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByUser(testUser)).thenReturn(Optional.of(testToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            forgetPassService.changePassword("test@example.com", "newPassword");
        });

        assertEquals("OTP not verified", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).findByUser(testUser);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testIntegration_CompletePasswordResetFlow() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepo).deleteByUser(testUser);
        
        // Mock OTP creation
        when(tokenRepo.save(any(PasswordToken.class))).thenAnswer(invocation -> {
            PasswordToken token = invocation.getArgument(0);
            token.setVerified(false);
            return token;
        });
        
        // Mock OTP verification
        when(tokenRepo.findByOtpAndUser(anyInt(), eq(testUser))).thenAnswer(invocation -> {
            Integer otp = invocation.getArgument(0);
            PasswordToken token = PasswordToken.builder()
                    .id(1L)
                    .otp(otp)
                    .expiryTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .verified(false)
                    .build();
            return Optional.of(token);
        });
        
        // Mock password change
        when(tokenRepo.findByUser(testUser)).thenAnswer(invocation -> 
            Optional.of(PasswordToken.builder()
                    .id(1L)
                    .otp(123456)
                    .expiryTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .verified(true)
                    .build())
        );
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");
        when(userRepo.save(testUser)).thenReturn(testUser);

        // Act - Complete password reset flow
        PasswordToken createdToken = forgetPassService.createOtp("test@example.com");
        assertDoesNotThrow(() -> forgetPassService.verifyOtp("test@example.com", createdToken.getOtp()));
        assertDoesNotThrow(() -> forgetPassService.changePassword("test@example.com", "newPassword123"));

        // Verify all interactions
        verify(userRepo, times(3)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).deleteByUser(testUser);
        verify(tokenRepo, times(2)).save(any(PasswordToken.class));
        verify(tokenRepo, times(1)).findByOtpAndUser(anyInt(), eq(testUser));
        verify(tokenRepo, times(1)).findByUser(testUser);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepo, times(1)).save(testUser);
        verify(tokenRepo, times(1)).delete(any(PasswordToken.class));
    }

    @Test
    void testCreateOtp_DeletesExistingTokens() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepo).deleteByUser(testUser);
        when(tokenRepo.save(any(PasswordToken.class))).thenAnswer(invocation -> {
            PasswordToken token = invocation.getArgument(0);
            return token;
        });

        // Act
        forgetPassService.createOtp("test@example.com");

        // Assert - Verify that existing tokens are deleted before creating new one
        verify(tokenRepo, times(1)).deleteByUser(testUser);
    }

    @Test
    void testVerifyOtp_SavesVerifiedToken() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepo.findByOtpAndUser(123456, testUser)).thenReturn(Optional.of(testToken));

        // Act
        forgetPassService.verifyOtp("test@example.com", 123456);

        // Assert - Verify that the token is saved after being marked as verified
        verify(tokenRepo, times(1)).save(testToken);
        assertTrue(testToken.isVerified());
    }
}