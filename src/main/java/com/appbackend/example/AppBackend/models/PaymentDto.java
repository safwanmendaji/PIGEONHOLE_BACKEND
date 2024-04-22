package com.appbackend.example.AppBackend.models;

import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.DisbursementsType;
import lombok.Data;

@Data
public class PaymentDto {
	private String account;
	private float amount;
	private String reference;
	private String narration;
	private String disbursementType;
	private DisbursementsStatus paymentStatus;
	private DisbursementsType disbursementsType;
	private Integer userId;





}