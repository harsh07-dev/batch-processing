package com.assignment.customer_batch_processor.validator;

public class ValidationException extends RuntimeException{

    public ValidationException(String message) {
        super(message);
    }
}
