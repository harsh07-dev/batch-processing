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
	private Long id;	
	private String name;
	private String email;
	private String phoneNumber;
	private String state;
	private String city;
	private String aadhaarNumber;
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