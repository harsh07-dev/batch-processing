package com.assignment.customer_batch_processor.validator;

public class RetryException extends RuntimeException{

    public RetryException(String message, Exception e) {
        super(message);
    }
}
