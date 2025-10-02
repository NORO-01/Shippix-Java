package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.Email.EmailTemplate;
import com.shippix.User_Management.Email.EmailTemplateFactory;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Service.BusinessOwnerRequestService;
import com.shippix.User_Management.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final BusinessOwnerRequestService requestService;
    private final EmailService emailService;
    private final EmailTemplateFactory emailTemplateFactory;

    @GetMapping
    public ResponseEntity<List<BOResponse>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BOResponse>> getByStatus(@PathVariable BusinessOwnerRequest.Status status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            BusinessOwner approvedBO = requestService.approveRequest(id);

            // Send approval email using interface
            EmailTemplate approvalEmail = emailTemplateFactory.createApprovalEmail(
                    approvedBO.getEmail(),
                    approvedBO.getBusinessName()
            );
            emailService.sendEmail(approvalEmail);

            return ResponseEntity.ok(approvedBO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            BOResponse rejectedRequest = requestService.rejectRequest(id);

            // Send rejection email using interface
            EmailTemplate rejectionEmail = emailTemplateFactory.createRejectionEmail(
                    rejectedRequest.email(),
                    rejectedRequest.businessName()
            );
            emailService.sendEmail(rejectionEmail);

            return ResponseEntity.ok(rejectedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}


