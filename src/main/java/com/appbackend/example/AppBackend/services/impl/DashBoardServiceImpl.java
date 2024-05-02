package com.appbackend.example.AppBackend.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.KycStatus;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appbackend.example.AppBackend.services.DashBoardService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;

import javax.swing.text.html.Option;

@Service
public class DashBoardServiceImpl implements DashBoardService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private KYCRepository kycRepository;

	@Autowired
	private CreditScoreService creditScoreService;

	@Autowired
	private CreditScoreRepository creditScoreRepository;

	@Autowired
	private KycCalculationDetailsRepository kycCalculationDetailsRepository;

	@Autowired
	private LoanEligibilityRepository loanEligibilityRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserLoanEligibilityRepository userLoanEligibilityRepository;

	@Autowired
	private UtilizeUserCreditRepository utilizeUserCreditRepository;

	@Override
	public ResponseEntity<?> getAllUsers() {
		try {
			List<User> users = userRepository.findAll();
			List<UserDto> userDtos = users.stream().map(user -> {
				int userId = user.getId();
				String firstName = user.getFirstName();
				String lastName = user.getLastName();
				String mobile = user.getPhoneNumber();
				String email = user.getEmail();
				Boolean isApproved = user.getIsApproved();

				Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(userId);
				int score = optionalCreditScore.map(CreditScore::getTotalCreditScore).orElse(0);

				return new UserDto(userId, firstName, lastName, mobile, email, score, isApproved == null ? false : isApproved);
			}).collect(Collectors.toList());
			SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success")
					.message("DATA GET SUCCESSFULLY.").data(userDtos).build();

			return ResponseEntity.status(HttpStatus.OK).body(successDto);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("error")
					.message("Internal Server Error").build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
		}
	}


	@Override
	public ResponseEntity<?> getUserAndKYCByUserId(int userId) {
		try {
			Optional<User> optionalUser = userRepository.findById(userId);
			if (optionalUser.isPresent()) {
				User user = optionalUser.get();
				Optional<KYC> optionalKYC = kycRepository.findById(userId);
				if (optionalKYC.isPresent()) {
					KYC kyc = optionalKYC.get();

					// Extract user details
					String firstName = user.getFirstName();
					String lastName = user.getLastName();
					String mobile = user.getPhoneNumber();
					String email = user.getEmail();
					boolean isApproved = user.getIsApproved();

					// Extract KYC details
					String dob = kyc.getDob();
					String address = kyc.getAddress();
					String maritalStatus = kyc.getMaritalStatus();
					String kin = kyc.getKin();
					String kinNumber = kyc.getKinNumber();
					String kin1 = kyc.getKin1();
					String kin1Number = kyc.getKin1Number();
					String nationalId = kyc.getNationalId();
					String gender = kyc.getGender();
					String age = kyc.getAge();

					byte[] documentData = kyc.getDocumentData();
					byte[] userImage = kyc.getUserImage();
					byte[] digitalSignature = kyc.getDigitalSignature();

					// Extract credit score details
					Integer reschedule = null;
					Integer score = null;
					Integer occupation = null;
					Integer departments = null;
					Integer security = null;
					Integer loanhistorycompletedloanswithoutarrears = null;
					Integer loanhistorycompletedloanswitharrearsnegative = null;
					Integer arrearsamountdefault = null;
					Integer daysinarrearspaymenthistory = null;
					Integer blackList = null;

					Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(userId);
					if (optionalCreditScore.isPresent()) {
						CreditScore creditScore = optionalCreditScore.get();
						score = creditScore.getTotalCreditScore();
						reschedule = creditScore.getRescheduledHistory() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getRescheduledHistory()) : null;
						occupation = creditScore.getOccupation() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getOccupation()) : null;
						departments = creditScore.getWorkPlaceDepartment() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getWorkPlaceDepartment()) : null;
						security = creditScore.getSecurity() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getSecurity()) : null;
						loanhistorycompletedloanswitharrearsnegative = creditScore.getLoanHistoryLoansWithArrears() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithArrears()) : null;
						loanhistorycompletedloanswithoutarrears = creditScore.getLoanHistoryLoansWithOutArrears() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithOutArrears()) : null;
						arrearsamountdefault = creditScore.getAmountInArrears() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getAmountInArrears()) : null;
						daysinarrearspaymenthistory = creditScore.getDaysInArrears() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getDaysInArrears()) : null;
						blackList = creditScore.getBlacklisted() != null ? CreditScoreService.findOldKycCalculationIdValue(creditScore.getBlacklisted()) : null;
					}

					// Construct UserKYCDto
					UserKYCDto userKYCDto = new UserKYCDto(
							firstName, lastName, mobile, email, score, isApproved, dob, address, maritalStatus, kin, kinNumber, kin1, kin1Number,
							nationalId, gender, age, documentData, userImage, digitalSignature, reschedule, occupation, departments, security,
							loanhistorycompletedloanswitharrearsnegative, loanhistorycompletedloanswithoutarrears, arrearsamountdefault,
							daysinarrearspaymenthistory, blackList
					);

					// Retrieve user loan eligibility
					Optional<UserLoanEligibility> loanEligibilityOptional = userLoanEligibilityRepository.getByUserId(userId);
					if (loanEligibilityOptional.isPresent()) {
						UserLoanEligibility userLoanEligibility = loanEligibilityOptional.get();
						userKYCDto.setLoanEligibility(userLoanEligibility.getEligibility().getId());
						userKYCDto.setEligibilityAmount(userLoanEligibility.getEligibilityAmount());
					}

					userKYCDto.setUserId(userId);
					SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success")
							.message("SUCCESS.").data(userKYCDto).build();
					return ResponseEntity.status(HttpStatus.OK).body(successDto);
				} else {
					SuccessDto successDto = SuccessDto.builder().code(HttpStatus.NOT_FOUND.value()).status("Error").message("KYC not found for the user").build();
					return ResponseEntity.status(HttpStatus.OK).body(successDto);
				}
			} else {
				SuccessDto successDto = SuccessDto.builder().code(HttpStatus.NOT_FOUND.value()).status("Error")
						.message("USER NOT FOUND").build();
				return ResponseEntity.status(HttpStatus.OK).body(successDto);
			}
		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR")
					.message("Internal server error").build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
		}
	}


	@Transactional
	public ResponseEntity<?> updateUserKyc(UserKYCDto userKycDto) {
		try {
			boolean updateEligibilityAmount = false;
			Optional<KYC> optionalKyc = kycRepository.findById(userKycDto.getUserId());
			if (optionalKyc.isPresent()) {

				creditScoreService.getCreditScore(userKycDto);

					if (userKycDto.getLoanEligibility() != 0 && userKycDto.getEligibilityAmount() != null) {
						UserLoanEligibility userLoanEligibility;

						User user = userRepository.findByid(userKycDto.getUserId())
								.orElseThrow(() -> new UsernameNotFoundException("User not found with this id: " + userKycDto.getUserId()));
						LoanEligibility loanEligibility = loanEligibilityRepository.findById(userKycDto.getLoanEligibility())
								.orElseThrow(() -> new UsernameNotFoundException("LoanEligibility not found with this id: " + userKycDto.getLoanEligibility()));

						Optional<UserLoanEligibility> loanEligibilityOptional = userLoanEligibilityRepository.getByUserId(user.getId());
						long oldEligibilityAmount = 0;
						if(loanEligibilityOptional.isPresent()) {
							userLoanEligibility = loanEligibilityOptional.get();
							oldEligibilityAmount = userLoanEligibility.getEligibilityAmount();
						} else {
							userLoanEligibility = new UserLoanEligibility();
						}
						userLoanEligibility.setEligibility(loanEligibility);
						userLoanEligibility.setUser(user);
						if(userKycDto.getEligibilityAmount() == oldEligibilityAmount){
							userLoanEligibility.setEligibilityAmount(userKycDto.getEligibilityAmount());
						}else{
							userLoanEligibility.setOldEligibilityAmount(oldEligibilityAmount);
							userLoanEligibility.setEligibilityAmount(userKycDto.getEligibilityAmount());
							updateEligibilityAmount = true;
						}
						userLoanEligibility = userLoanEligibilityRepository.save(userLoanEligibility);

						UtilizeUserCredit userCredit = utilizeUserCreditRepository.findLatestByUserIdOrderByCreditScoreDescDesc(user.getId());
						if (userCredit == null){
							userCredit = new UtilizeUserCredit();
							userCredit.setUserLoanEligibility(userLoanEligibility);
							userCredit.setAvailableBalance(userKycDto.getEligibilityAmount().doubleValue());
							userCredit.setUtilizeBalance(0.0);
							userCredit.setUser(user);
							utilizeUserCreditRepository.save(userCredit);
						}else if(updateEligibilityAmount){
							long increaseAmount = userKycDto.getEligibilityAmount() - oldEligibilityAmount;
							double availableAmount = userCredit.getAvailableBalance();
							userCredit.setAvailableBalance(availableAmount + increaseAmount);
							utilizeUserCreditRepository.save(userCredit);
						}

					}
					SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("Success")
							.message("KYC UPDATED SUCCESSFULLY.").build();
					return ResponseEntity.status(HttpStatus.OK).body(successDto);


					} else {
				SuccessDto successDto = SuccessDto.builder().code(HttpStatus.NOT_FOUND.value()).status("Error")
						.message("KYC RECORDS NOT FOUND FOR ID : . " + userKycDto.getUserId()).build();
				return ResponseEntity.status(HttpStatus.OK).body(successDto);
			}
		} catch (Exception e) {
			e.printStackTrace();
			SuccessDto successDto = SuccessDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("Error")
					.message("SOME THING WENT WRONG. " + e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(successDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> enableDisEnabledUser(ApprovalDeclineDto dto) {
		try {
			Optional<User> optionalUser = userService.getUserById(dto.getId());
			if (!optionalUser.isPresent()) {
				ErrorDto errorResponse = ErrorDto.builder()
						.code(HttpStatus.NOT_FOUND.value())
						.status("Error")
						.message("User not found")
						.build();
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}
			User user = optionalUser.get();
			Boolean isApproved = user.getIsApproved();

			if (isApproved == null) {
				ErrorDto errorResponse = ErrorDto.builder()
						.code(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.status("Error")
						.message("Approval status is null for user with ID " + dto.getId())
						.build();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
			}

			user.setIsApproved(!isApproved);
			Optional<KYC> optionalKyc = kycRepository.findByUserId(dto.getId());

			KYC kyc = optionalKyc.get();

			if (user.getIsApproved()) {
				kyc.setStatus(String.valueOf(KycStatus.APPROVED));
			} else {
				kyc.setStatus(String.valueOf(KycStatus.DECLINED));
				kyc.setReason(dto.getReason());
			}

			kycRepository.save(kyc);
			userRepository.save(user);

			String status = isApproved ? "disabled" : "enabled";
			SuccessDto successResponse = SuccessDto.builder()
					.code(HttpStatus.OK.value())
					.status("Success")
					.message("User with ID " + dto.getId() + " has been " + status)
					.build();
			return ResponseEntity.status(HttpStatus.OK).body(successResponse);
		} catch (Exception e) {
			// Log the exception for debugging purposes
			e.printStackTrace();

			// Return an appropriate error response
			ErrorDto errorResponse = ErrorDto.builder()
					.code(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.status("Error")
					.message("Internal Server Error")
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}


}
