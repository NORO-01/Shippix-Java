package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Service.BusinessOwnerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final BusinessOwnerRequestService requestService;

    @GetMapping
    public ResponseEntity<List<BOResponse>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BOResponse>> getByStatus(@PathVariable BusinessOwnerRequest.Status status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BusinessOwner> approve(
            @PathVariable Long id,
            @RequestParam String password
    ) {
        return ResponseEntity.ok(requestService.approveRequest(id, password));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BOResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.rejectRequest(id));
    }
}


