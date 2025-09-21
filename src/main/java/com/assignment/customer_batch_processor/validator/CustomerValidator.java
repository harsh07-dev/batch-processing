package com.assignment.customer_batch_processor.validator;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Customer Data Validator with Regex Patterns
 * 
 * Validates all customer fields according to the specified requirements:
 * - Name → (alphabet)
 * - Mobile → (Indian 10-digit starting with 6/7/8/9)  
 * - Email → (as per emailId)
 * - Aadhaar → (as per Aadhaar number)
 * - PAN → (5 letters, 4 digits, 1 letter)
 * - State → (alphabet)
 * - City → (alphabet)
 */
@Component
@Slf4j
public class CustomerValidator {
    
    
    // Regex patterns for validation as per requirements
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}\\d{4}[A-Z]{1}$");
    private static final Pattern STATE_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern CITY_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    
    /**
     * Validates all fields of a customer record
     * @param customer Customer object to validate
     * @return true if all validations pass, false otherwise
     */
    public boolean validateCustomer(Customer customer) throws Exception{
        if (customer == null) {
            log.warn(" VALIDATOR: Customer object is null");
            return false;
        }
        
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();
        
        // Validate Name (alphabets and spaces only)
        if (!isValidName(customer.getName())) {
            isValid = false;
            errors.append("Invalid name format; ");
        }
        
        // Validate Mobile Number (Indian 10-digit starting with 6/7/8/9)
        if (!isValidMobile(customer.getPhoneNumber())) {
            isValid = false;
            errors.append("Invalid mobile number format; ");
        }
        
        // Validate Email (standard email format)
        if (!isValidEmail(customer.getEmail())) {
            isValid = false;
            errors.append("Invalid email format; ");
        }
        
        // Validate Aadhaar (12 digits)
        if (!isValidAadhaar(customer.getAadhaarNumber())) {
            isValid = false;
            errors.append("Invalid Aadhaar number format; ");
        }
        
        // Validate PAN (5 letters, 4 digits, 1 letter)
        if (!isValidPAN(customer.getPanNumber())) {
            isValid = false;
            errors.append("Invalid PAN number format; ");
        }
        
        // Validate State (alphabets and spaces only)
        if (!isValidState(customer.getState())) {
            isValid = false;
            errors.append("Invalid state format; ");
        }
        
        // Validate City (alphabets and spaces only)
        if (!isValidCity(customer.getCity())) {
            isValid = false;
            errors.append("Invalid city format; ");
        }
        
        if (!isValid) {
            log.debug("VALIDATOR: Validation failed for customer '{}': {}",
                        customer.getName(), errors.toString().trim());
        } else {
            log.debug("VALIDATOR: All validations passed for customer: {}", customer.getName());
        }
        
        return isValid;
    }
    
    /**
     * Validates name field - only alphabets and spaces allowed
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validates Indian mobile number - 10 digits starting with 6, 7, 8, or 9
     */
    public boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }
    
    /**
     * Validates email address format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates Aadhaar number - exactly 12 digits
     */
    public boolean isValidAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.trim().isEmpty()) {
            return false;
        }
        return AADHAAR_PATTERN.matcher(aadhaar.trim()).matches();
    }
    
    /**
     * Validates PAN number - 5 letters, 4 digits, 1 letter (e.g., ABCDE1234F)
     */
    public boolean isValidPAN(String pan) {
        if (pan == null || pan.trim().isEmpty()) {
            return false;
        }
        return PAN_PATTERN.matcher(pan.trim()).matches();
    }
    
    /**
     * Validates state name - only alphabets and spaces allowed
     */
    public boolean isValidState(String state) {
        if (state == null || state.trim().isEmpty()) {
            return false;
        }
        return STATE_PATTERN.matcher(state.trim()).matches();
    }
    
    /**
     * Validates city name - only alphabets and spaces allowed
     */
    public boolean isValidCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        return CITY_PATTERN.matcher(city.trim()).matches();
    }
}