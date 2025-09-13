package com.shippix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shippix.User_Management.DTO.NewPassRequest;
import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import com.shippix.User_Management.Service.PasswordService;
import com.shippix.User_Management.Service.UserService;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordTokenRepo passwordTokenRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService; 

   // Test data
    private final Users testUser;
    private final PasswordToken validToken;
    private final PasswordToken expiredToken;

    {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("username");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("oldPassword");
        testUser.setRole(Users.Role.ROLE_BUSINESS_OWNER);

        validToken = PasswordToken.builder()
                .token("valid-token")
                .user(testUser)
                .expiryTime(new Date(System.currentTimeMillis() + 100000)) // valid for 100 seconds
                .build();

        expiredToken = PasswordToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryTime(new Date(System.currentTimeMillis() - 100000)) // expired 100 seconds ago
                .build();
    }


    // Password test cases - using the actual NewPassRequest record
    private final NewPassRequest validPassword = new NewPassRequest("ValidPass123!", "ValidPass123!");
    private final NewPassRequest matchingPasswords = new NewPassRequest("newPassword123!", "newPassword123!");
    private final NewPassRequest nonMatchingPasswords = new NewPassRequest("newPassword123!", "differentPassword");
    private final NewPassRequest blankPassword = new NewPassRequest("", "");
    private final NewPassRequest nullPassword = new NewPassRequest(null, null);
    private final NewPassRequest shortPassword = new NewPassRequest("Short1!", "Short1!");
    private final NewPassRequest noDigitPassword = new NewPassRequest("NoDigit!", "NoDigit!");
    private final NewPassRequest noSpecialCharPassword = new NewPassRequest("NoSpecial123", "NoSpecial123");

    // setPassword tests

    @Test
    void setPassword_WithValidTokenAndValidPassword_ShouldUpdatePasswordAndDeleteToken() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("encodedPassword");
        when(userRepo.save(testUser)).thenReturn(testUser);
        doNothing().when(passwordTokenRepo).delete(validToken);

        // Act
        passwordService.setPassword("valid-token", validPassword);

        // Assert
        assertEquals("encodedPassword", testUser.getPassword());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verify(passwordEncoder, times(1)).encode("ValidPass123!");
        verify(userRepo, times(1)).save(testUser);
        verify(passwordTokenRepo, times(1)).delete(validToken);
    }

    @Test
    void setPassword_WithBlankPassword_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", blankPassword);
        });

        assertEquals("Password must not be blank", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithNullPassword_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", nullPassword);
        });

        assertEquals("Password must not be blank", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithShortPassword_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", shortPassword);
        });

        assertEquals("Password must be at least 8 characters long", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithPasswordWithoutDigit_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", noDigitPassword);
        });

        assertEquals("Password must contain at least one numeric digit", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithPasswordWithoutSpecialChar_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", noSpecialCharPassword);
        });

        assertEquals("Password must contain at least one special character", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithInvalidToken_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("invalid-token", validPassword);
        });

        assertEquals("Invalid or expired token", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("invalid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_WithExpiredToken_ShouldDeleteTokenAndThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        doNothing().when(passwordTokenRepo).delete(expiredToken);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("expired-token", validPassword);
        });

        assertEquals("Token expired", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("expired-token");
        verify(passwordTokenRepo, times(1)).delete(expiredToken);
        verifyNoInteractions(passwordEncoder, userRepo);
    }

    @Test
    void setPassword_WithNonMatchingPasswords_ShouldThrowException() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            passwordService.setPassword("valid-token", nonMatchingPasswords);
        });

        assertEquals("Passwords do not match", exception.getMessage());
        verify(passwordTokenRepo, times(1)).findByToken("valid-token");
        verifyNoInteractions(passwordEncoder, userRepo);
        verify(passwordTokenRepo, never()).delete(any());
    }

    @Test
    void setPassword_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword123!")).thenReturn("encodedNewPassword");
        when(userRepo.save(testUser)).thenReturn(testUser);
        doNothing().when(passwordTokenRepo).delete(validToken);

        // Act
        passwordService.setPassword("valid-token", matchingPasswords);

        // Assert
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void setPassword_WithValidPasswordContainingMultipleSpecialChars_ShouldWork() {
        // Test multiple special characters
        NewPassRequest multipleSpecialChars = new NewPassRequest("Pass123@#$", "Pass123@#$");
        
        when(passwordTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("Pass123@#$")).thenReturn("encodedPassword");
        when(userRepo.save(testUser)).thenReturn(testUser);
        doNothing().when(passwordTokenRepo).delete(validToken);

        // Act
        passwordService.setPassword("valid-token", multipleSpecialChars);

        // Assert
        verify(passwordEncoder, times(1)).encode("Pass123@#$");
    }

    // changePassword tests with password validation

    @Test
    void changePassword_WithValidPassword_ShouldUpdatePassword() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("encodedNewPassword");
        when(userRepo.save(testUser)).thenReturn(testUser);

        // Act
        userService.changePassword("user@example.com", "ValidPass123!", "oldPassword");

        // Assert
        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verify(passwordEncoder, times(1)).encode("ValidPass123!");
        verify(userRepo, times(1)).save(testUser);
    }

    @Test
    void changePassword_WithBlankPassword_ShouldThrowException() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("user@example.com", "", "oldPassword");
        });

        assertEquals("Password must not be blank", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_WithNullPassword_ShouldThrowException() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("user@example.com", "ValidPass123!", null);
        });

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_WithShortPassword_ShouldThrowException() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("user@example.com", "Short1!", "oldPassword");
        });

        assertEquals("Password must be at least 8 characters long", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_WithPasswordWithoutDigit_ShouldThrowException() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("user@example.com", "NoDigit!", "oldPassword");
        });

        assertEquals("Password must contain at least one numeric digit", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_WithPasswordWithoutSpecialChar_ShouldThrowException() {
        // Arrange
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("user@example.com", "NoSpecial123", "oldPassword");
        });

        assertEquals("Password must contain at least one special character", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("user@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_WithNonExistentEmail_ShouldThrowException() {
        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.changePassword("nonexistent@example.com", "ValidPass123!", "oldPassword");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("nonexistent@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepo, never()).save(any());
    }

}


// i think the issue is UserService uses BCryptPasswordEncoder, which not matching passwords correctly