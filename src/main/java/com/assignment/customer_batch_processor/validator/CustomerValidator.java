package com.assignment.customer_batch_processor.validator;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CustomerValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerValidator.class);
    
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
    public boolean validateCustomer(Customer customer) {
        if (customer == null) {
            logger.warn("❌ VALIDATOR: Customer object is null");
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
            logger.debug("⚠️ VALIDATOR: Validation failed for customer '{}': {}", 
                        customer.getName(), errors.toString().trim());
        } else {
            logger.debug("✅ VALIDATOR: All validations passed for customer: {}", customer.getName());
        }
        
        return isValid;
    }
    
    /**
     * Validates name field - only alphabets and spaces allowed
     * Example: "JOHN DOE" ✅, "JOHN123" ❌
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validates Indian mobile number - 10 digits starting with 6, 7, 8, or 9
     * Example: "9876543210" ✅, "1234567890" ❌, "98765432101" ❌
     */
    public boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }
    
    /**
     * Validates email address format
     * Example: "john.doe@example.com" ✅, "invalid-email" ❌
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates Aadhaar number - exactly 12 digits
     * Example: "123456789012" ✅, "12345678901" ❌, "ABCD12345678" ❌
     */
    public boolean isValidAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.trim().isEmpty()) {
            return false;
        }
        return AADHAAR_PATTERN.matcher(aadhaar.trim()).matches();
    }
    
    /**
     * Validates PAN number - 5 letters, 4 digits, 1 letter (e.g., ABCDE1234F)
     * Example: "ABCDE1234F" ✅, "AB1234567C" ❌, "ABCDEFGHIJ" ❌
     */
    public boolean isValidPAN(String pan) {
        if (pan == null || pan.trim().isEmpty()) {
            return false;
        }
        return PAN_PATTERN.matcher(pan.trim()).matches();
    }
    
    /**
     * Validates state name - only alphabets and spaces allowed
     * Example: "MAHARASHTRA" ✅, "UTTAR PRADESH" ✅, "STATE123" ❌
     */
    public boolean isValidState(String state) {
        if (state == null || state.trim().isEmpty()) {
            return false;
        }
        return STATE_PATTERN.matcher(state.trim()).matches();
    }
    
    /**
     * Validates city name - only alphabets and spaces allowed
     * Example: "MUMBAI" ✅, "NEW DELHI" ✅, "CITY123" ❌
     */
    public boolean isValidCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        return CITY_PATTERN.matcher(city.trim()).matches();
    }
    
    /**
     * Get validation statistics for reporting
     */
    public ValidationStats validateAndGetStats(Customer customer) {
        boolean nameValid = isValidName(customer.getName());
        boolean mobileValid = isValidMobile(customer.getPhoneNumber());
        boolean emailValid = isValidEmail(customer.getEmail());
        boolean aadhaarValid = isValidAadhaar(customer.getAadhaarNumber());
        boolean panValid = isValidPAN(customer.getPanNumber());
        boolean stateValid = isValidState(customer.getState());
        boolean cityValid = isValidCity(customer.getCity());
        
        return new ValidationStats(nameValid, mobileValid, emailValid, aadhaarValid, 
                                 panValid, stateValid, cityValid);
    }
    
    /**
     * Validation statistics holder
     */
    public static class ValidationStats {
        private final boolean nameValid;
        private final boolean mobileValid;
        private final boolean emailValid;
        private final boolean aadhaarValid;
        private final boolean panValid;
        private final boolean stateValid;
        private final boolean cityValid;
        
        public ValidationStats(boolean nameValid, boolean mobileValid, boolean emailValid,
                             boolean aadhaarValid, boolean panValid, boolean stateValid, 
                             boolean cityValid) {
            this.nameValid = nameValid;
            this.mobileValid = mobileValid;
            this.emailValid = emailValid;
            this.aadhaarValid = aadhaarValid;
            this.panValid = panValid;
            this.stateValid = stateValid;
            this.cityValid = cityValid;
        }
        
        public boolean isAllValid() {
            return nameValid && mobileValid && emailValid && aadhaarValid && 
                   panValid && stateValid && cityValid;
        }
        
        public int getInvalidCount() {
            int count = 0;
            if (!nameValid) count++;
            if (!mobileValid) count++;
            if (!emailValid) count++;
            if (!aadhaarValid) count++;
            if (!panValid) count++;
            if (!stateValid) count++;
            if (!cityValid) count++;
            return count;
        }
        
        // Getters
        public boolean isNameValid() { return nameValid; }
        public boolean isMobileValid() { return mobileValid; }
        public boolean isEmailValid() { return emailValid; }
        public boolean isAadhaarValid() { return aadhaarValid; }
        public boolean isPanValid() { return panValid; }
        public boolean isStateValid() { return stateValid; }
        public boolean isCityValid() { return cityValid; }
    }
}