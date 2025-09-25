package com.shippix.User_Management.DTO;

import com.shippix.User_Management.Model.Users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private String role;
    private String phoneNumber;
    private String profileImage;

    public static UserDTO fromUser(Users user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getPhoneNumber(),
                user.getProfileImage()
        );
    }
}
