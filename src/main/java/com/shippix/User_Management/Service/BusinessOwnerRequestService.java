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

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessOwnerRequestService {

    private final BusinessOwnerRequestRepo requestRepo;
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder;

    // Submit a new request (registration flow)
    public BOResponse submitRequest(BORequest dto) {
        BusinessOwnerRequest request = new BusinessOwnerRequest();
        request.setName(dto.name());
        request.setEmail(dto.email());
        request.setPhoneNumber(dto.phoneNumber());
        request.setNationalId(dto.nationalId());
        request.setBusinessName(dto.businessName());
        request.setBusinessType(dto.businessType());
        request.setLatitude(dto.latitude());
        request.setLongitude(dto.longitude());
        request.setStatus(BusinessOwnerRequest.Status.PENDING);

        BusinessOwnerRequest saved = requestRepo.save(request);
        return toResponse(saved);
    }

    // Approve a request and create a BusinessOwner user
    public BusinessOwner approveRequest(Long requestId, String rawPassword) {
        BusinessOwnerRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BusinessOwnerRequest.Status.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        request.setStatus(BusinessOwnerRequest.Status.APPROVED);
        requestRepo.save(request);

        BusinessOwner bo = new BusinessOwner();
        bo.setUsername(request.getEmail());
        bo.setEmail(request.getEmail());
        bo.setPhoneNumber(request.getPhoneNumber());
        bo.setPassword(encoder.encode(rawPassword));
        bo.setRole(Users.Role.ROLE_BUSINESS_OWNER);
        bo.setBusinessName(request.getBusinessName());
        bo.setBusinessType(request.getBusinessType());
        bo.setNationalId(request.getNationalId());
        bo.setLatitude(request.getLatitude());
        bo.setLongitude(request.getLongitude());

        return userRepo.save(bo);
    }

    // Reject a request
    public BOResponse rejectRequest(Long requestId) {
        BusinessOwnerRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

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


