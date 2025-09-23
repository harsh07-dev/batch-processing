package com.assignment.customer_batch_processor.repository;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByEmail(String email);
}
