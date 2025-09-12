package com.shippix.User_Management.DTO;

public record PassChangeRequest(String oldPassword, String newPassword, String repeatPassword) {}

