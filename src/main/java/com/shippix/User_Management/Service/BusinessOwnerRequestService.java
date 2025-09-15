package com.shippix.User_Management.Service;

import com.shippix.User_Management.DTO.BORequest;
import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.BusinessOwnerRequestRepo;
import com.shippix.User_Management.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessOwnerRequestService {

    private final BusinessOwnerRequestRepo requestRepo;
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;


    // Submit a new request (registration flow)
    public BOResponse submitRequest(BORequest dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        if (userRepo.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        if (requestRepo.findByStatus(BusinessOwnerRequest.Status.PENDING).stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(dto.email()))) {
            throw new IllegalArgumentException("A pending request already exists for this email");
        }
        if (requestRepo.findByStatus(BusinessOwnerRequest.Status.APPROVED).stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(dto.email()))) {
            throw new IllegalArgumentException("This email has already been approved");
        }

        BusinessOwnerRequest request = new BusinessOwnerRequest();
        request.setName(dto.name());
        request.setEmail(dto.email());
        request.setPhoneNumber(dto.phoneNumber());
        request.setNationalId(dto.nationalId());
        request.setBusinessName(dto.businessName());
        request.setBusinessType(dto.businessType());
        request.setLatitude(dto.latitude());
        request.setLongitude(dto.longitude());
        request.setPassword(passwordEncoder.encode(dto.password()));
        request.setStatus(BusinessOwnerRequest.Status.PENDING);

        BusinessOwnerRequest saved = requestRepo.save(request);
        return toResponse(saved);
    }

//    // Submit a new request (registration flow)
//    public BOResponse submitRequest(BORequest dto) {
//        BusinessOwnerRequest request = new BusinessOwnerRequest();
//        request.setName(dto.name());
//        request.setEmail(dto.email());
//        request.setPhoneNumber(dto.phoneNumber());
//        request.setNationalId(dto.nationalId());
//        request.setBusinessName(dto.businessName());
//        request.setBusinessType(dto.businessType());
//        request.setLatitude(dto.latitude());
//        request.setLongitude(dto.longitude());
//        request.setStatus(BusinessOwnerRequest.Status.PENDING);
//
//        BusinessOwnerRequest saved = requestRepo.save(request);
//        return toResponse(saved);
//    }

    // Approve a request and create BusinessOwner account
    @Transactional
    public BusinessOwner approveRequest(Long requestId) {
        BusinessOwnerRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));


        if (request.getStatus() != BusinessOwnerRequest.Status.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("User already exists with this email");
        }

        request.setStatus(BusinessOwnerRequest.Status.APPROVED);
        requestRepo.save(request);

        BusinessOwner bo = new BusinessOwner();
        bo.setUsername(request.getName());
        bo.setEmail(request.getEmail());
        bo.setPhoneNumber(request.getPhoneNumber());
        bo.setRole(Users.Role.ROLE_BUSINESS_OWNER);
        bo.setBusinessName(request.getBusinessName());
        bo.setBusinessType(request.getBusinessType());
        bo.setNationalId(request.getNationalId());
        bo.setLatitude(request.getLatitude());
        bo.setLongitude(request.getLongitude());
        bo.setPassword(request.getPassword());

        userRepo.save(bo);
        return bo;
    }

    // Approve a request and send password setup link
//    @Transactional
//    public BusinessOwner approveRequest(Long requestId) throws MessagingException {
//        BusinessOwnerRequest request = requestRepo.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Request not found"));
//
//        if (request.getStatus() != BusinessOwnerRequest.Status.PENDING) {
//            throw new RuntimeException("Request already processed");
//        }
//
//        request.setStatus(BusinessOwnerRequest.Status.APPROVED);
//        requestRepo.save(request);
//
//        BusinessOwner bo = new BusinessOwner();
//        bo.setUsername(request.getEmail());
//        bo.setEmail(request.getEmail());
//        bo.setPhoneNumber(request.getPhoneNumber());
//        bo.setRole(Users.Role.ROLE_BUSINESS_OWNER);
//        bo.setBusinessName(request.getBusinessName());
//        bo.setBusinessType(request.getBusinessType());
//        bo.setNationalId(request.getNationalId());
//        bo.setLatitude(request.getLatitude());
//        bo.setLongitude(request.getLongitude());
//
//        String tempPassword = "TEMP_PASSWORD_" + UUID.randomUUID().toString().substring(0, 8);
//        bo.setPassword(passwordEncoder.encode(tempPassword));
//
//        userRepo.save(bo);
//
//        String token = UUID.randomUUID().toString();
//        PasswordToken setupToken = PasswordToken.builder()
//                .token(token)
//                .user(bo)
//                .expiryTime(new Date(System.currentTimeMillis() + 1000*60*60*24*3))
//                .build();
//
//        passwordTokenRepo.save(setupToken);
//
//        String link = "https://localhost:8080/set-password?token=" + token;
//        EmailBody email = EmailBody.builder()
//                .to(bo.getEmail())
//                .subject("Set up your Shippix account password")
//                .text("Welcome to Shippix! Please set up your password by clicking the link below.")
//                .link(link)
//                .build();
//
//        emailService.sendEmail(email);
//        return bo;
//    }

    // Reject a request
    public BOResponse rejectRequest(Long requestId) {
        BusinessOwnerRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BusinessOwnerRequest.Status.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        request.setStatus(BusinessOwnerRequest.Status.REJECTED);
        BusinessOwnerRequest saved = requestRepo.save(request);

        return toResponse(saved);
    }

    // List all requests
    public List<BOResponse> getAllRequests() {
        return requestRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // List by status
    public List<BOResponse> getRequestsByStatus(BusinessOwnerRequest.Status status) {
        return requestRepo.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    // Mapping helper
    private BOResponse toResponse(BusinessOwnerRequest r) {
        return new BOResponse(
                r.getId(),
                r.getName(),
                r.getEmail(),
                r.getBusinessName(),
                r.getBusinessType(),
                r.getStatus()
        );
    }
}

