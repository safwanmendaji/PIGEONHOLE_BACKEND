package com.appbackend.example.AppBackend.models;

import lombok.Data;

@Data
public class CreditScoreDtoDemo {
	private int blacklisted;

	private int workPlaceDepartment;

	private int occupation;

	private int salaryScale;

	private int amountInArrears;

	private int daysInArreas;

	private int rescheduleHistory;

	private int priorityClient;

	private int security;

	private int loanHistoryLoansWithArrears;
	
	private int loanHistoryLoansWithOutArrears;

}
