package com.shippix.User_Management.Repo;

import com.shippix.User_Management.Model.BusinessOwnerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BusinessOwnerRequestRepo extends JpaRepository<BusinessOwnerRequest, Long> {
    List<BusinessOwnerRequest> findByStatus(BusinessOwnerRequest.Status status);
}


