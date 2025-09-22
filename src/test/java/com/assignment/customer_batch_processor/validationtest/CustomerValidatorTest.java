package com.assignment.customer_batch_processor.validationtest;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.validator.CustomerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerValidatorTest {

    private CustomerValidator validator;

    @BeforeEach
    void setup() {
        validator = new CustomerValidator();
    }

    // ---------------------------
    // Test full customer validation
    // ---------------------------
    @Test
    void testValidateCustomer_ValidCustomer_ReturnsTrue() throws Exception {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setPhoneNumber("9876543210");
        customer.setEmail("john.doe@example.com");
        customer.setAadhaarNumber("123456789012");
        customer.setPanNumber("ABCDE1234F");
        customer.setState("Karnataka");
        customer.setCity("Bangalore");

        assertTrue(validator.validateCustomer(customer));
    }

    @Test
    void testValidateCustomer_InvalidFields_ReturnsFalse() throws Exception {
        Customer customer = new Customer();
        customer.setName("John123");                  // invalid name
        customer.setPhoneNumber("12345");             // invalid mobile
        customer.setEmail("john@com");                // invalid email
        customer.setAadhaarNumber("123");             // invalid Aadhaar
        customer.setPanNumber("12345ABCDE");          // invalid PAN
        customer.setState("Karnataka1");              // invalid state
        customer.setCity("Bangalore1");               // invalid city

        assertFalse(validator.validateCustomer(customer));
    }

    @Test
    void testValidateCustomer_NullCustomer_ReturnsFalse() throws Exception {
        assertFalse(validator.validateCustomer(null));
    }

    // ---------------------------
    // Individual field tests
    // ---------------------------
    @Test
    void testIsValidName() {
        assertTrue(validator.isValidName("Alice Smith"));
        assertFalse(validator.isValidName("Alice123"));
        assertFalse(validator.isValidName(""));
        assertFalse(validator.isValidName(null));
    }

    @Test
    void testIsValidMobile() {
        assertTrue(validator.isValidMobile("9876543210"));
        assertFalse(validator.isValidMobile("1234567890"));
        assertFalse(validator.isValidMobile("98765"));
        assertFalse(validator.isValidMobile(null));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(validator.isValidEmail("test@example.com"));
        assertFalse(validator.isValidEmail("test.com"));
        assertFalse(validator.isValidEmail(""));
        assertFalse(validator.isValidEmail(null));
    }

    @Test
    void testIsValidAadhaar() {
        assertTrue(validator.isValidAadhaar("123456789012"));
        assertFalse(validator.isValidAadhaar("12345"));
        assertFalse(validator.isValidAadhaar(null));
    }

    @Test
    void testIsValidPAN() {
        assertTrue(validator.isValidPAN("ABCDE1234F"));
        assertFalse(validator.isValidPAN("1234ABCDE1"));
        assertFalse(validator.isValidPAN(null));
    }

    @Test
    void testIsValidState() {
        assertTrue(validator.isValidState("Maharashtra"));
        assertFalse(validator.isValidState("Maharashtra123"));
        assertFalse(validator.isValidState(""));
    }

    @Test
    void testIsValidCity() {
        assertTrue(validator.isValidCity("Pune"));
        assertFalse(validator.isValidCity("Pune123"));
        assertFalse(validator.isValidCity(null));
    }
}

