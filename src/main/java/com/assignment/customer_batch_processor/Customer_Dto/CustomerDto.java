package com.assignment.customer_batch_processor.Customer_Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
	
	@NotBlank(message = "Name cannot be blank.")
    private String name;

    // Validates that the email is a valid format and not blank.
    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email should be a valid email address.")
    private String email;

    // Validates that the phone number is 10 digits and contains only numbers.
    @NotNull(message = "Phone number cannot be null.")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain only digits.")
    private String phoneNumber;

    // Validates that the state is not null or an empty string.
    @NotBlank(message = "State cannot be blank.")
    private String state;

    // Validates that the city is not null or an empty string.
    @NotBlank(message = "City cannot be blank.")
    private String city;

    // Validates that the Aadhaar number is a 12-digit number.
    @NotNull(message = "Aadhaar number cannot be null.")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be a 12-digit number.")
    private String aadhaarNumber;

    // Validates that the PAN number follows the standard format.
    @NotNull(message = "PAN number cannot be null.")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number should be in the format ABCDE1234F.")
    private String panNumber;

}
