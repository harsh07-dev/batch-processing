package com.assignment.customer_batch_processor.testpackage;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.Utilities.CustomerItemProcessor;
import com.assignment.customer_batch_processor.service.EncryptionService;
import com.assignment.customer_batch_processor.validator.CustomerValidator;
import com.assignment.customer_batch_processor.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerItemProcessorTest {

    private CustomerItemProcessor processor;
    private CustomerValidator validatorMock;
    private EncryptionService encryptionServiceMock;

    @BeforeEach
    void setup() {
        validatorMock = mock(CustomerValidator.class);
        encryptionServiceMock = mock(EncryptionService.class);

        processor = new CustomerItemProcessor();
        processor.customerValidator = validatorMock;
        processor.encryptionService = encryptionServiceMock;
    }

    @Test
    void testProcess_ValidCustomer_ReturnsProcessedCustomer() throws ValidationException {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("9876543210");
        customer.setAadhaarNumber("123456789012");
        customer.setPanNumber("ABCDE1234F");
        customer.setState("Karnataka");
        customer.setCity("Bangalore");

        when(validatorMock.isValidName(anyString())).thenReturn(true);
        when(validatorMock.isValidEmail(anyString())).thenReturn(true);
        when(validatorMock.isValidMobile(anyString())).thenReturn(true);
        when(validatorMock.isValidAadhaar(anyString())).thenReturn(true);
        when(validatorMock.isValidPAN(anyString())).thenReturn(true);
        when(validatorMock.isValidState(anyString())).thenReturn(true);
        when(validatorMock.isValidCity(anyString())).thenReturn(true);

        Customer result = processor.process(customer);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhoneNumber());
        assertEquals("123456789012", result.getAadhaarNumber());
        assertEquals("ABCDE1234F", result.getPanNumber());
        assertEquals("KARNATAKA", result.getState());
        assertEquals("BANGALORE", result.getCity());
       // assertNotNull(result.getCreatedDate());
    }

    @Test
    void testProcess_InvalidName_ThrowsValidationException() {
        Customer customer = new Customer();
        customer.setName("John123");

        when(validatorMock.isValidName("John123")).thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> processor.process(customer)
        );

        assertTrue(exception.getMessage().contains("Invalid name"));
    }
}
