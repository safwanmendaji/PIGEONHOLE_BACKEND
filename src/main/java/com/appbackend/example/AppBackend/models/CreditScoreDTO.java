package com.appbackend.example.AppBackend.models;

import lombok.Data;

@Data
public class CreditScoreDTO {

	private Integer totalCreditScore;
	private long availableOffer;
	private long totalExposure;
	private long offerPerLevel;

}
