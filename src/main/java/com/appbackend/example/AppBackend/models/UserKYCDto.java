package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserKYCDto extends  CreditScoreDtoDemo{


	private Integer userId;
	private boolean isApproved;
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
	private int loanEligibility;
	private Double eligibilityAmount;

	public UserKYCDto(int reschedule ,  int occupation,int departments,
					  int security, int loanhistorycompletedloanswithoutarrears,
					  int loanhistorycompletedloanswitharrearsnegative, int arrearsamountdefault, int daysinarrearspaymenthistor,
					  int blacklisted

	) {
		this.workPlaceDepartment = departments;
		this.occupation = occupation;
		this.rescheduleHistory = reschedule;
		this.loanHistoryLoansWithOutArrears = loanhistorycompletedloanswithoutarrears;
		this.security = security;
		this.amountInArrears = arrearsamountdefault;
		this.loanHistoryLoansWithArrears = loanhistorycompletedloanswitharrearsnegative;
		this.daysInArreas = daysinarrearspaymenthistor;
		this.blacklisted = blacklisted;
	}

}
