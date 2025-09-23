


package com.assignment.customer_batch_processor.Utilities;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.repository.CustomerRepository;
import com.assignment.customer_batch_processor.service.EncryptionService;
import com.assignment.customer_batch_processor.validator.RetryException;
import com.assignment.customer_batch_processor.validator.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * WRITER COMPONENT
 * Receive processed Customer objects from Processor
 * Save customers to database in chunks
 * Handle database transactions
 * Log writing progress and statistics
 * Handle database errors gracefully
 */
@Component
@Slf4j
public class CustomerItemWriter implements ItemWriter<Customer> {

    @PersistenceContext
    public EntityManager entityManager;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    public EncryptionService encryptionService;

    // Updated static variables for retry testing
    private static int totalProcessed = 0;
   // private static boolean hasFailedOnce = false;

   // private static int attemptCount = 0;

    @Override
    @Transactional
    public void write(Chunk<? extends Customer> chunk) throws Exception {

        List<? extends Customer> customers = chunk.getItems();
        int chunkSize = customers.size();

        log.info("WRITER: Writing chunk of {} customers to database. Total so far: {} ", chunkSize, totalProcessed);

        /**
         * Test to check if retry is working.
         */
//        if (totalProcessed >= 2 && attemptCount < 2) {
//            attemptCount++;
//            log.info("RETRY TEST: Simulating failure after processing {} records", totalProcessed);
//            throw new RetryException("Simulated failure for retry testing after " + totalProcessed, new RuntimeException("cause"));
//        }


        for (Customer customer : customers) {
            try {
                // Your existing code...
                if (customer.getAadhaarNumber() != null) {
                    String encryptedAadhaar = encryptionService.encrypt(customer.getAadhaarNumber());
                    customer.setAadhaarNumber(encryptedAadhaar);
                    log.debug(" WRITER: Aadhaar encrypted successfully");
                }

                if (customer.getPanNumber() != null) {
                    String encryptedPan = encryptionService.encrypt(customer.getPanNumber());
                    customer.setPanNumber(encryptedPan);
                    log.debug(" WRITER: PAN encrypted successfully");
                }
                //isDuplicateCustomer(customer);
                setAuditFields(customer);
                saveCustomer(customer);


                totalProcessed++;
                log.info("WRITER: Saved customer #{} - Name: {}", totalProcessed, customer.getName());

            } catch (Exception e) {
                log.info("WRITER: Failed to save customer {}: {}", customer.getName(), e.getMessage());
                throw new RetryException("Exception in write data {} " + e.getMessage(), e);
            }
        }

        // Flush changes to database
        try {
            customerRepository.flush();
            entityManager.clear(); // Clear persistence context to free memory
        } catch (Exception e) {
            log.info("WRITER: Error flushing entity manager: {}", e.getMessage());
            throw new RetryException("Exception in write data {} " + e.getMessage(), e);
        }

        log.info("Successfully wrote {} customers. Total processed: {}", chunkSize, totalProcessed);
    }


    private void setAuditFields(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedDate(now);
        customer.setUpdatedDate(null); // New record, so no update date
    }

    /**
     * STEP : Save customer to database
     */
    private void saveCustomer(Customer customer) throws Exception {
        try {
            customerRepository.save(customer);
        } catch (Exception e) {
            log.error("Exception in save customer");
            throw new Exception("Exception in save Customer " + e.getMessage(), e);
        }
    }

    /**
     * i am checking if my customer is having the duplicate entry just to test.
     */
//    public void isDuplicateCustomer(Customer customer) throws RetryException {
//        try {
//            Optional<Customer> c = customerRepository.findByEmail(customer.getEmail());
//
//            if (c.isPresent()) {
//                log.info("is duplicate email present {} ", c.get().getEmail());
//                throw new ValidationException("Customer All ready exist" + c.get().getName());
//            }
//        } catch (Exception e) {
//            log.error("Duplicate customer exist");
//            throw new org.springframework.retry.RetryException("Customer All ready exist" + customer.getName());
//        }
}

