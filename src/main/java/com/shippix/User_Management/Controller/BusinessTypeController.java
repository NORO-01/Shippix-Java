package com.shippix.User_Management.Controller;

import com.shippix.User_Management.Model.BusinessType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/business-types")
public class BusinessTypeController {

    public record BusinessTypeDTO(String value, String displayName) {}

    @GetMapping
    public List<BusinessTypeDTO> list() {
        return Arrays.stream(BusinessType.values())
                .map(bt -> new BusinessTypeDTO(bt.name(), bt.getDisplayName()))
                .toList();
    }
}