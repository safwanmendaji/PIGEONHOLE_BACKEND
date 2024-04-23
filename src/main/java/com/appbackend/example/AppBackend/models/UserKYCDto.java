package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserKYCDto extends  CreditScoreDtoDemo{

//	public UserKYCDto(String firstName2, String mobile2, String email2, int score2, boolean isApproved2, String dob2,
//			String address2, String maritalStatus2, String kin2, String kinNumber2, String kin12, String kin1Number2,
//			String nationalId2, String gender2, String age2, byte[] documentData2, byte[] userImage2,
//			byte[] digitalSignature2) {
//		// TODO Auto-generated constructor stub
//	}

	private Integer userId;
	private String firstName;
	private String mobile;
	private String email;
	private int score;
	private boolean isApproved;
	private String dob;
	private String address;
	private String maritalStatus;
	private String kin;
	private String kinNumber;
	private String kin1;
	private String kin1Number;
	private String nationalId;
	private String gender;
	private String age;

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

	private byte[] documentData;

	private byte[] userImage;

	private byte[] digitalSignature;

	public UserKYCDto(String firstName, String mobile, String email, int score, boolean isApproved, String dob, String address,
					  String maritalStatus, String kin, String kinNumber, String kin1, String kin1Number, String nationalId,
					  String gender, String age,byte[] documentData,
					  byte[] userImage, byte[] digitalSignature , int reschedule ,  int occupation,int departments,
					  int security, int loanhistorycompletedloanswithoutarrears,
					  int loanhistorycompletedloanswitharrearsnegative, int arrearsamountdefault, int daysinarrearspaymenthistor,
					  int blacklisted

	) {
		this.firstName = firstName;
		this.mobile = mobile;
		this.email = email;
		this.score = score;
		this.isApproved = isApproved;
		this.dob = dob;
		this.address = address;
		this.maritalStatus = maritalStatus;
		this.kin = kin;
		this.kinNumber = kinNumber;
		this.kin1 = kin1;
		this.kin1Number = kin1Number;
		this.nationalId = nationalId;
		this.gender = gender;
		this.age = age;
		this.workPlaceDepartment = departments;
		this.occupation = occupation;
		this.rescheduleHistory = reschedule;
		this.loanHistoryLoansWithOutArrears = loanhistorycompletedloanswithoutarrears;
		this.security = security;
		this.amountInArrears = arrearsamountdefault;
		this.loanHistoryLoansWithArrears = loanhistorycompletedloanswitharrearsnegative;
		this.daysInArreas = daysinarrearspaymenthistor;
		this.documentData = documentData;
		this.userImage = userImage;
		this.digitalSignature = digitalSignature;
		this.blacklisted = blacklisted;
	}

}