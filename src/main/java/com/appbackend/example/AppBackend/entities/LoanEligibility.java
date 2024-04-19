package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="loan_eligibility")
public class LoanEligibility {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name="eligibility_level")
	private String eligibilityLevel;
	
	@Column(name="start_amount")
	private long startAmount;
	
	@Column(name="end_amount")
	private long endAmount;
	
	
	@Column(name="quality")
	private String quality;
	
	@Column(name="qualification_payment_history")
	private String qualificationPaymentHistory;
	
	@Column(name="custom_starting_level")
	private String customStartingLevel;
}
