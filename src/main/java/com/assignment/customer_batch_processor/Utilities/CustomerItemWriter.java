
package com.assignment.customer_batch_processor.Utilities;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.service.EncryptionService;
import com.assignment.customer_batch_processor.validator.RetryException;
import com.assignment.customer_batch_processor.validator.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
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
    public EntityManager entityManager;

    @Autowired
    public EncryptionService encryptionService;

   // private static int attemptCount = 0;
   private static int totalProcessed = 0;
    private static boolean hasFailedOnce = false;

    @Override
    @Transactional
    public void write(Chunk<? extends Customer> chunk) throws Exception {

        List<? extends Customer> customers = chunk.getItems();
        int chunkSize = customers.size();



        log.info("WRITER: Writing chunk of {} customers to database Attempt: Total so far: {} " ,chunkSize, totalProcessed);

        //SIMPLE RESTART TEST: Fail after processing N records, only on first attempt


//        if (totalProcessed >5 && !hasFailedOnce) {
//            hasFailedOnce = true;
//            log.info("RESTART TEST:Simulating failure after processing {} records", totalProcessed);
//            log.info("my Restart is working fine");
//            RuntimeException e=new RuntimeException("cause");
//            throw new RetryException("Simulated failure for restart testing after " + totalProcessed ,e);
//        }

        for (Customer customer : customers) {
            try {

                if (customer.getAadhaarNumber() != null) {
            String encryptedAadhaar = encryptionService.encrypt(customer.getAadhaarNumber());
            customer.setAadhaarNumber(encryptedAadhaar);
            log.info(" PROCESSOR: Aadhaar encrypted successfully");
                }

                if (customer.getPanNumber() != null) {
            String encryptedPan = encryptionService.encrypt(customer.getPanNumber());
            customer.setPanNumber(encryptedPan);
          log.info(" PROCESSOR: PAN encrypted successfully");
                }

//               if (isDuplicateCustomer(customer)){
//                   log.info(" PROCESSOR: Duplicate email found for customer: {}", customer.getEmail());
//                   throw new ValidationException("Duplicate email: " + customer.getEmail());
//               }

                setAuditFields(customer);

                // STEP:Save to database
                saveCustomer(customer);
                //totalProcessed++;
                log.info("WRITER: Saved customer #{} - Name: {}", totalProcessed, customer.getName());

            } catch (Exception e) {
                log.error("WRITER: Failed to save customer {}: {}", customer.getName(), e.getMessage());
                throw new RetryException("Exception in write data {} " + e.getMessage(), e);
            }
        }
        totalProcessed += chunkSize;
        // Flush changes to database
        try {
            entityManager.flush();
            entityManager.clear(); // Clear persistence context to free memory
        } catch (Exception e) {
            log.error("WRITER: Error flushing entity manager: {}", e.getMessage());
            throw new RetryException("Exception in write data {} " + e.getMessage(), e);
        }

        log.info("Successfully wrote {} customers. Total processed: {}", chunkSize, totalProcessed);

    }

    /**
     * Set audit fields for customer
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

    // Check if customer already exists (by email)

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