package com.shippix.User_Management.Controller;

import com.shippix.User_Management.Model.BusinessType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business-types")
public class BusinessTypeController {

    @GetMapping
    public List<BusinessTypeInfo> getAllBusinessTypes() {
        return List.of(BusinessType.values()).stream()
                .map(type -> new BusinessTypeInfo(type.name(), type.getDisplayName()))
                .toList();
    }

    public record BusinessTypeInfo(String value, String displayName) {}
}