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
 * Receive Customer objects from Reader
 * Validate all fields using regex patterns
 * Clean and normalize data
 * Pass valid records to Writer
 */
@Component
@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {
    @Autowired
    public CustomerValidator customerValidator;
    @Autowired
    public EncryptionService encryptionService;
    private int processedCount = 0;

    @Override
    public Customer process(Customer customer) throws ValidationException {
        processedCount++;
        log.info(" PROCESSOR: Processing customer #{} - {}", processedCount, customer.getName());

        try {
            // STEP: Clean and normalize data
            Customer customerFromCsv = cleanAndBuildCustomer(customer);

            // STEP: Set audit timestamps
            customerFromCsv.setCreatedDate(java.time.LocalDateTime.now());
            customerFromCsv.setUpdatedDate(null);

            logProgress();
            log.info(" PROCESSOR: Successfully processed customer - Name: {}", customerFromCsv.getName());

            return customerFromCsv;

        } catch (ValidationException ve) {
            log.info(" PROCESSOR: Error processing customer {}: {}", customer.getName(), ve.getMessage());
            throw ve;
        }
    }

    private Customer cleanAndBuildCustomer(Customer customer) {
        log.info(" PROCESSOR: Cleaning customer data");



        // Name


        if (customer.getName() != null) {
            if (customerValidator.isValidName(customer.getName())) {
                customer.setName(customer.getName().trim());
            } else {
                throw new ValidationException("Invalid name " + customer.getName());
            }
        }

        // Email
        if (customer.getEmail() != null) {
            if (customerValidator.isValidEmail(customer.getEmail())) {

                customer.setEmail(customer.getEmail().trim().toLowerCase());
            } else {
                throw new ValidationException("Invalid email for name " + customer.getName());
            }
        }

        // Phone
        if (customer.getPhoneNumber() != null) {
            if (customerValidator.isValidMobile(customer.getPhoneNumber())) {
                customer.setPhoneNumber(customer.getPhoneNumber().trim().replaceAll("[^0-9]", ""));
            } else {
                throw new ValidationException("Invalid phone number for name " + customer.getName());
            }
        }

        // Aadhaar
        if (customer.getAadhaarNumber() != null) {
            if (customerValidator.isValidAadhaar(customer.getAadhaarNumber())) {
                customer.setAadhaarNumber(customer.getAadhaarNumber().trim().replaceAll("[^0-9]", ""));
            } else {
                throw new ValidationException("Invalid Aadhaar for name " + customer.getName());
            }
        }

        // PAN
        if (customer.getPanNumber() != null) {
            if (customerValidator.isValidPAN(customer.getPanNumber())) {
                customer.setPanNumber(customer.getPanNumber().trim().toUpperCase());
            } else {
                throw new ValidationException("Invalid PAN for name " + customer.getName());
            }
        }

        // State
        if (customer.getState() != null) {
            if (customerValidator.isValidState(customer.getState())) {
                customer.setState(customer.getState().trim().toUpperCase());
            } else {
                throw new ValidationException("Invalid state for name " + customer.getName());
            }
        }

        // City
        if (customer.getCity() != null) {
            if (customerValidator.isValidCity(customer.getCity())) {
                customer.setCity(customer.getCity().trim().toUpperCase());
            } else {
                throw new ValidationException("Invalid city for name " + customer.getName());
            }
        }



        return customer;
    }









    //  Logs progress every 1000 records

    private void logProgress() {
        if (processedCount % 1000 == 0) {
            log.info(" PROCESSOR: Progress - Processed: {}", processedCount);
        }
    }


}
