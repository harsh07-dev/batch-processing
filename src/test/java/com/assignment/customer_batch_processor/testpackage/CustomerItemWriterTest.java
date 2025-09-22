package com.assignment.customer_batch_processor.testpackage;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.Utilities.CustomerItemWriter;
import com.assignment.customer_batch_processor.service.EncryptionService;
import com.assignment.customer_batch_processor.validator.RetryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityManager;
import org.springframework.batch.item.Chunk;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerItemWriterTest {

    private CustomerItemWriter writer;
    private EntityManager entityManagerMock;
    private EncryptionService encryptionServiceMock;

    @BeforeEach
    void setup() {
        entityManagerMock = mock(EntityManager.class);
        encryptionServiceMock = mock(EncryptionService.class);

        writer = new CustomerItemWriter();
        writer.entityManager = entityManagerMock;
        writer.encryptionService = encryptionServiceMock;
    }

    @Test
    void testWrite_ValidCustomers_Success() throws Exception {
        // Prepare sample customers
        Customer customer1 = new Customer();
        customer1.setName("John Doe");
        customer1.setAadhaarNumber("123456789012");
        customer1.setPanNumber("ABCDE1234F");

        Customer customer2 = new Customer();
        customer2.setName("Alice Smith");
        customer2.setAadhaarNumber("987654321098");
        customer2.setPanNumber("XYZAB5678C");

        List<Customer> customers = Arrays.asList(customer1, customer2);
        Chunk<Customer> chunk = new Chunk<>(customers);

        // Mock encryption
        when(encryptionServiceMock.encrypt("123456789012")).thenReturn("ENC_AADHAAR_1");
        when(encryptionServiceMock.encrypt("ABCDE1234F")).thenReturn("ENC_PAN_1");
        when(encryptionServiceMock.encrypt("987654321098")).thenReturn("ENC_AADHAAR_2");
        when(encryptionServiceMock.encrypt("XYZAB5678C")).thenReturn("ENC_PAN_2");

        // Execute
        writer.write(chunk);

        // Verify encryption called
        verify(encryptionServiceMock).encrypt("123456789012");
        verify(encryptionServiceMock).encrypt("ABCDE1234F");
        verify(encryptionServiceMock).encrypt("987654321098");
        verify(encryptionServiceMock).encrypt("XYZAB5678C");

        // Verify persist called
        verify(entityManagerMock, times(2)).persist(any(Customer.class));
        verify(entityManagerMock).flush();
        verify(entityManagerMock).clear();

        // Assert customer fields encrypted
        assertEquals("ENC_AADHAAR_1", customer1.getAadhaarNumber());
        assertEquals("ENC_PAN_1", customer1.getPanNumber());
        assertEquals("ENC_AADHAAR_2", customer2.getAadhaarNumber());
        assertEquals("ENC_PAN_2", customer2.getPanNumber());

        // Assert audit fields set
//        assertNotNull(customer1.getCreatedDate());
//        assertNull(customer1.getUpdatedDate());
//        assertNotNull(customer2.getCreatedDate());
//        assertNull(customer2.getUpdatedDate());
    }

    @Test
    void testWrite_EncryptionFails_ThrowsRetryException() throws Exception {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAadhaarNumber("123456789012");

        Chunk<Customer> chunk = new Chunk<>(List.of(customer));

        when(encryptionServiceMock.encrypt("123456789012")).thenThrow(new RuntimeException("Encryption error"));

        RetryException exception = assertThrows(
                RetryException.class,
                () -> writer.write(chunk)
        );

        assertTrue(exception.getMessage().contains("Exception in write data"));
    }

    @Test
    void testWrite_PersistFails_ThrowsRetryException() throws Exception {
        Customer customer = new Customer();
        customer.setName("John Doe");

        Chunk<Customer> chunk = new Chunk<>(List.of(customer));

        doThrow(new RuntimeException("DB error")).when(entityManagerMock).persist(customer);

        RetryException exception = assertThrows(
                RetryException.class,
                () -> writer.write(chunk)
        );

        assertTrue(exception.getMessage().contains("Exception in write data"));
    }
}
