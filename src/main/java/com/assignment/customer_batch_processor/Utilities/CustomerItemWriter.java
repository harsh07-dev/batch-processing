package com.assignment.customer_batch_processor.Utilities;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.validator.RetryException;
import com.assignment.customer_batch_processor.validator.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * WRITER COMPONENT
 * 
 * Responsibility:
 * 1. Receive processed Customer objects from Processor
 * 2. Save customers to database in chunks
 * 3. Handle database transactions
 * 4. Manage duplicate records
 * 5. Log writing progress and statistics
 * 6. Handle database errors gracefully
 */
@Component
@Slf4j
public class CustomerItemWriter implements ItemWriter<Customer> {
    
    @PersistenceContext
    EntityManager entityManager;
    
    @Override
    @Transactional
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        
        List<? extends Customer> customers = chunk.getItems();
        int chunkSize = customers.size();
        
        log.info("WRITER: Writing chunk of {} customers to database", chunkSize);
        
        for (Customer customer : customers) {
            try {
                // STEP : Set audit fields
                setAuditFields(customer);
                
//                if (isDuplicateCustomer(customer)) {
//                    throw new ValidationException("Email already exists for user " + customer.getName());
//                }
                
                // STEP : Save to database
                saveCustomer(customer);

                log.debug("WRITER: Saved customer - ID: {}, Name: {}, Email: {}",
                           customer.getId(), customer.getName(), customer.getEmail());
                
            } catch (Exception e) {
                log.error("WRITER: Failed to save customer {}: {}",
                           customer.getName(), e.getMessage());
                
                throw new RetryException("Exception in write data {} " + e.getMessage(), e);
            }
        }
        
        // STEP : Flush changes to database
        try {
            entityManager.flush();
            entityManager.clear(); // Clear persistence context to free memory
        } catch (Exception e) {
            log.error("WRITER: Error flushing entity manager: {}", e.getMessage());
            throw new RetryException("Exception in write data {} " + e.getMessage(), e);
        }
    }
    
    /**
     * STEP 1: Set audit fields for customer
     */
    private void setAuditFields(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedDate(now);
        customer.setUpdatedDate(null); // New record, so no update date
    }

    /**
     * STEP : Save customer to database
     */
    private void saveCustomer(Customer customer) {
        try {
            entityManager.persist(customer);
        } catch (Exception e) {
            log.error("Exception in save customer");
            throw new RetryException("Exception in save Customer " + e.getMessage(),e);
        }
    }

//    /**
//     //     *  Check if customer already exists (by email)
//     //     */
//    private boolean isDuplicateCustomer(Customer customer) {
//        try {
//            Long count = entityManager.createQuery(
//                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email", Long.class)
//                .setParameter("email", customer.getEmail())
//                .getSingleResult();
//
//            return count > 0;
//
//        } catch (Exception e) {
//            log.error("WRITER: Error checking for duplicate from DB: {}", e.getMessage());
//    return true;
//        }
//    }
}