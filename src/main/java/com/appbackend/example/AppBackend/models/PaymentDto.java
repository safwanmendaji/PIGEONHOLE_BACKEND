package com.appbackend.example.AppBackend.models;

import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.DisbursementsType;
import jakarta.persistence.Lob;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class PaymentDto {
	private String account;
	private float amount;
	private UUID reference;
	private String narration;
	private DisbursementsStatus paymentStatus;
	private DisbursementsType disbursementsType;
	private Integer userId;
	private StudentDetails studentDetails;
	private TravelDetails travelDetails;
	private String reason;

	@Lob
	private String document;

	@Data
	public class StudentDetails{
		private String studentCode;
		private String Name;
		private String schoolName;
		private String studentClass;
		private float outstandingFees;
	}

	@Data
	public class TravelDetails{
		private String teamLeadName;
		private String teamLeadContactNumber;
		private Date startDate;
		private Date endDate;
		private String destination;
	}


}