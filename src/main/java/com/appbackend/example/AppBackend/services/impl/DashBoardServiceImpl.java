package com.appbackend.example.AppBackend.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.models.UserDto;
import com.appbackend.example.AppBackend.models.UserKYCDto;
import com.appbackend.example.AppBackend.services.DashBoardService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;

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

	@Override
	public ResponseEntity<?> getAllUsers() {
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

			return new UserDto(userId, firstName,lastName, mobile, email, score, isApproved == null ? false : isApproved);
		}).collect(Collectors.toList());
		SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success")
				.message("DATA GET SUCCESSFULLY.").data(userDtos).build();

		return ResponseEntity.status(HttpStatus.OK).body(successDto);
	}

	@Override
	public ResponseEntity<?> getUserAndKYCByUserId(int userId) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
				Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(userId);
				int reschedule = 0;
				int occupation = 0;
				int departments = 0;
				int security = 0;
				int loanhistorycompletedloanswithoutarrears = 0;
				int loanhistorycompletedloanswitharrearsnegative = 0;
				int arrearsamountdefault = 0;
				int daysinarrearspaymenthistory = 0;
				int blackList = 0;
				if (optionalCreditScore.isPresent()) {
					CreditScore creditScore = optionalCreditScore.get();
					reschedule = creditScore.getRescheduledHistory() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getRescheduledHistory())
							: 0;
					occupation = creditScore.getOccupation() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getOccupation())
							: 0;
					departments = creditScore.getWorkPlaceDepartment() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getWorkPlaceDepartment())
							: 0;
					security = creditScore.getSecurity() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getSecurity())
							: 0;
					loanhistorycompletedloanswitharrearsnegative = creditScore.getLoanHistoryLoansWithArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithArrears())
							: 0;
					loanhistorycompletedloanswithoutarrears = creditScore.getLoanHistoryLoansWithOutArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithOutArrears())
							: 0;
					arrearsamountdefault = creditScore.getAmountInArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getAmountInArrears())
							: 0;
					daysinarrearspaymenthistory = creditScore.getDaysInArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getDaysInArrears())
							: 0;
					blackList = creditScore.getBlacklisted() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getBlacklisted())
							: 0;
				}

				UserKYCDto userKYCDto = new UserKYCDto(reschedule, occupation, departments, security,
						loanhistorycompletedloanswitharrearsnegative, loanhistorycompletedloanswithoutarrears,
						arrearsamountdefault, daysinarrearspaymenthistory, blackList);

				userKYCDto.setUserId(userId);
				SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success")
						.message("SUCCESS.").data(userKYCDto).build();
				return ResponseEntity.status(HttpStatus.OK).body(successDto);

		} else {
			SuccessDto successDto = SuccessDto.builder().code(HttpStatus.NOT_FOUND.value()).status("Error")
					.message("USER NOT FOUND").build();
			return ResponseEntity.status(HttpStatus.OK).body(successDto);
		}
	}

	@Transactional
	public ResponseEntity<?> updateUserKyc(UserKYCDto userKycDto) {
		try {
			Optional<KYC> optionalKyc = kycRepository.findById(userKycDto.getUserId());
			if (optionalKyc.isPresent()) {

				creditScoreService.getCreditScore(userKycDto);
				try {
					if (userKycDto.getLoanEligibility() != 0 && userKycDto.getEligibilityAmount() != null) {
						UserLoanEligibility userLoanEligibility = new UserLoanEligibility();
						Optional<LoanEligibility> LoanEligibilityOption = loanEligibilityRepository.findById(userKycDto.getLoanEligibility());
						Optional<User> userOptional = userRepository.findByid(userKycDto.getUserId());
						LoanEligibilityOption.ifPresent(userLoanEligibility::setEligibility);
						userOptional.ifPresent(userLoanEligibility::setUser);
						userLoanEligibility.setEligibilityAmount(userKycDto.getEligibilityAmount());
						userLoanEligibilityRepository.save(userLoanEligibility);

					}
				}catch (Exception e){
					e.printStackTrace();
					System.out.println("Error in userLoanEligibility ::   " + e.getMessage());
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
	public ResponseEntity<?> enableDisEnabledUser(int id) {
	    Optional<User> optionalUser = userService.getUserById(id);

	    if (!optionalUser.isPresent()) {
	        SuccessDto errorResponse = SuccessDto.builder()
	                .code(HttpStatus.NOT_FOUND.value())
	                .status("Error")
	                .message("User not found")
	                .build();
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	    }

	    User user = optionalUser.get();
	    Boolean isApproved = user.getIsApproved();

	    if (isApproved == null) {
	        SuccessDto errorResponse = SuccessDto.builder()
	                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
	                .status("Error")
	                .message("Approval status is null for user with ID " + id)
	                .build();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	    }

	    user.setIsApproved(!isApproved);

	    userRepository.save(user);

	    String status = isApproved ? "disabled" : "enabled";
	    SuccessDto successResponse = SuccessDto.builder()
	            .code(HttpStatus.OK.value())
	            .status("Success")
	            .message("User with ID " + id + " has been " + status)
	            .build();
	    return ResponseEntity.status(HttpStatus.OK).body(successResponse);
	}


}
