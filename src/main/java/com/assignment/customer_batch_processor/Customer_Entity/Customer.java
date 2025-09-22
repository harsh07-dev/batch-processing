package com.assignment.customer_batch_processor.Customer_Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false,nullable = false)
	private Long id;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "email", nullable = false, length = 150)
	private String email;

	@Column(name = "phone_number", nullable = false, length = 10)
	private String phoneNumber;

	@Column(name = "state", nullable = false, length = 100)
	private String state;

	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@Column(name = "aadhaar_number_encrypted", nullable = false, length = 500)
	private String aadhaarNumber;

	@Column(name = "pan_number_encrypted", nullable = false, length = 500)
	private String panNumber;

@Override
public String toString() {
    return "Customer{" +
            "name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", aadhaarNumber='" + aadhaarNumber + '\'' +
            ", panNumber='" + panNumber + '\'' +
            ", state='" + state + '\'' +
            ", city='" + city + '\'' +
            '}';
}











public void setCreatedDate(LocalDateTime now) {
	// TODO Auto-generated method stub
	
}


public void setUpdatedDate(Object object) {
	// TODO Auto-generated method stub
	
}
}