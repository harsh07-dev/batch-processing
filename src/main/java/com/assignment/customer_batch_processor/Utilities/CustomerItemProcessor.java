package com.assignment.customer_batch_processor.Utilities;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.service.EncryptionService;
import com.assignment.customer_batch_processor.validator.CustomerValidator;
import com.assignment.customer_batch_processor.validator.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * PROCESSOR COMPONENT
 * 
 * Responsibility:
 * 1. Receive Customer objects from Reader
 * 2. Validate all fields using regex patterns
 * 3. Clean and normalize data
 * 4. Encrypt sensitive data (Aadhaar & PAN)
 * 5. Generate UUID for primary key
 * 6. Filter out invalid records (return null)
 * 7. Pass valid records to Writer
 */
@Component
@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {
    
    @Autowired
    CustomerValidator customerValidator;
    
    @Autowired
    EncryptionService encryptionService;
    
    private int processedCount = 0;
    
    @Override
    public Customer process(Customer customer) throws ValidationException {
        processedCount++;
        
        log.info(" PROCESSOR: Processing customer #{} - {}", processedCount, customer.getName());
        
        try {
            // STEP 1: read data from csv, clean and build it.
            Customer customerFromCsv = cleanAndBuildCustomer(customer);
            
            // STEP 2: Encrypt sensitive data
            encryptSensitiveData(customerFromCsv);
            
            // STEP 3: Set audit timestamps (createdDate will be set in @PrePersist)
            // Just ensure the methods exist and work
            customerFromCsv.setCreatedDate(java.time.LocalDateTime.now());
            customerFromCsv.setUpdatedDate(null); // New record, no update date
            logProgress();
            
            log.info(" PROCESSOR: Successfully processed customer - Name: {}",
                        customerFromCsv.getName());
            
            return customerFromCsv;
            
        } catch (ValidationException ve) {
            log.error(" PROCESSOR: Error processing customer {}: {}",
                    customer.getName(), ve.getMessage());
            throw ve;
        }
    }
    
    /**
     * STEP 1: Clean and normalize customer data
     */
    private Customer cleanAndBuildCustomer(Customer customer) {
        log.info(" PROCESSOR: Cleaning customer data");
        
        if (customer.getName() != null) {
            if(customerValidator.isValidName(customer.getName())) {
                customer.setName(customer.getName().trim());
            } else {
                throw new ValidationException("Invalid name {} "+ customer.getName());
            }
        }
        
        if (customer.getEmail() != null) {
            if(customerValidator.isValidEmail(customer.getEmail())) {
                customer.setEmail(customer.getEmail().trim().toLowerCase());
            }
            else {
                throw new ValidationException("Invalid email for name {} "+ customer.getName());
            }
        }
        
        if (customer.getPhoneNumber() != null) {
            // Remove all spaces and special characters
            if(customerValidator.isValidMobile(customer.getPhoneNumber())) {
                customer.setPhoneNumber(customer.getPhoneNumber().trim().replaceAll("[^0-9]", ""));
            }
            else {
                throw new ValidationException("Invalid phone number for name {} "+ customer.getName());
            }
        }

        if (customer.getAadhaarNumber() != null) {
            // Remove spaces and special characters
            if(customerValidator.isValidAadhaar(customer.getAadhaarNumber())) {
                customer.setAadhaarNumber(customer.getAadhaarNumber().trim().replaceAll("[^0-9]", ""));
            } else {
                throw new ValidationException("Invalid aadhar for name {} "+ customer.getName());
            }
        }
        
        if (customer.getPanNumber() != null) {
            if(customerValidator.isValidPAN(customer.getPanNumber())) {
                customer.setPanNumber(customer.getPanNumber().trim().toUpperCase());
            } else {
                throw new ValidationException("Invalid Pan for name "+ customer.getName() + ".");
            }
        }
        
        if (customer.getState() != null) {
            if(customerValidator.isValidState(customer.getState())) {
                customer.setState(customer.getState().trim().toUpperCase());
            }
            else {
                throw new ValidationException("Invalid state for name {} "+ customer.getName());
            }
        }
        
        if (customer.getCity() != null) {
            if(customerValidator.isValidCity(customer.getCity())) {
                customer.setCity(customer.getCity().trim().toUpperCase());
            } else {
                throw new ValidationException("Invalid city for name {} "+ customer.getName());
            }
        }
        
        return customer;
    }

    /**
     * STEP 4: Encrypt sensitive data (Aadhaar & PAN)
     */
    private void encryptSensitiveData(Customer customer) throws ValidationException {
        log.info(" PROCESSOR: Encrypting sensitive data");
        
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
    }
    
    /**
     * Log processing progress
     */
    private void logProgress() {
        if (processedCount % 1000 == 0) {
            log.info(" PROCESSOR: Progress - Processed: {} ",
                       processedCount);
        }
    }

}