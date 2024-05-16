package com.appbackend.example.AppBackend.services.impl;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.KycStatus;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appbackend.example.AppBackend.services.DashBoardService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Log
@Service
@Slf4j
public class DashBoardServiceImpl implements DashBoardService {


	@Autowired
	private RestTemplate restTemplate;


	@Autowired
	private AppCommon appCommon;

	@Autowired
	public void DashboardService(KYCRepository kycRepository, DisbursementsRepository disbursementHistoryRepository,
								 UtilizeUserCreditRepository utilizeUserCreditRepository, UserRepository userRepository) {
		this.kycRepository = kycRepository;
		this.disbursementHistoryRepository = disbursementHistoryRepository;
		this.utilizeUserCreditRepository = utilizeUserCreditRepository;
		this.userRepository = userRepository;
	}


	@Autowired
	private DisbursementsRepository disbursementHistoryRepository;

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

	@Autowired
	private DisbursementsRepository disbursementsHistoryRepository;



	@Override
	@Transactional
	public ResponseEntity<?> getAllUsers() {
		List<User> users = userRepository.findAll();
		List<UserDto> userDtos = getUserDtos(users , false);

		SuccessDto successDto = SuccessDto.builder()
				.code(HttpStatus.OK.value())
				.status("success")
				.message("DATA GET SUCCESSFULLY.")
				.data(userDtos)
				.build();

		return ResponseEntity.status(HttpStatus.OK).body(successDto);
	}

	@Override
	public ResponseEntity<?> getUserAndKYCByUserId(int userId) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			Optional<KYC> optionalKYC = kycRepository.findById(userId);
			if (optionalKYC.isPresent()) {
				KYC kyc = optionalKYC.get();

				String firstName = user.getFirstName();
				String lastName = user.getLastName();
				String mobile = user.getPhoneNumber();
				String email = user.getEmail();
				boolean isApproved = user.getIsApproved();

				// Retrieve additional fields from KYC
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
				String status = kyc.getStatus();

				String documentData = kyc.getDocumentData();
				String userImage = kyc.getUserImage();
				String digitalSignature = kyc.getDigitalSignature();


				Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(userId);
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
				Integer priorityClient = null;


				if (optionalCreditScore.isPresent()) {
					CreditScore creditScore = optionalCreditScore.get();
					score = creditScore.getTotalCreditScore();
					reschedule = creditScore.getRescheduledHistory() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getRescheduledHistory())
							: null;
					occupation = creditScore.getOccupation() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getOccupation())
							: null;
					departments = creditScore.getWorkPlaceDepartment() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getWorkPlaceDepartment())
							: null;
					security = creditScore.getSecurity() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getSecurity())
							: null;
					loanhistorycompletedloanswitharrearsnegative = creditScore.getLoanHistoryLoansWithArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithArrears())
							: null;
					loanhistorycompletedloanswithoutarrears = creditScore.getLoanHistoryLoansWithOutArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getLoanHistoryLoansWithOutArrears())
							: null;
					arrearsamountdefault = creditScore.getAmountInArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getAmountInArrears())
							: null;
					daysinarrearspaymenthistory = creditScore.getDaysInArrears() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getDaysInArrears())
							: null;
					blackList = creditScore.getBlacklisted() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getBlacklisted())
							: null;
					priorityClient = creditScore.getPriorityClient() != null
							? CreditScoreService.findOldKycCalculationIdValue(creditScore.getPriorityClient())
							: null;
				}

//				UserKYCDto userKYCDto = new UserKYCDto(reschedule, occupation, departments, security,
//						loanhistorycompletedloanswitharrearsnegative, loanhistorycompletedloanswithoutarrears,
//						arrearsamountdefault, daysinarrearspaymenthistory, blackList);

				Optional<UserLoanEligibility> loanEligibilityOptional = userLoanEligibilityRepository.getByUserId(userId);
				UserLoanEligibility userLoanEligibility = loanEligibilityOptional.isPresent() ? loanEligibilityOptional.get() : null;


				UserKYCDto userKYCDto = new UserKYCDto(
						firstName, lastName, mobile, email, score, isApproved, dob, address, maritalStatus, kin, kinNumber, kin1, kin1Number,
						nationalId, gender, age, documentData, userImage, digitalSignature, reschedule, occupation, departments, security,
						loanhistorycompletedloanswitharrearsnegative, loanhistorycompletedloanswithoutarrears, arrearsamountdefault,
						daysinarrearspaymenthistory, blackList, status , priorityClient
				);

				if (userLoanEligibility != null) {
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
	}

	@Transactional
	public ResponseEntity<?> updateUserKyc(UserKYCDto userKycDto) {
		try {
			boolean updateEligibilityAmount = false;
			String message = "";
			Optional<KYC> optionalKyc = kycRepository.findById(userKycDto.getUserId());
			if (optionalKyc.isPresent()) {
				creditScoreService.getCreditScore(userKycDto);
				CreditScore creditScore = creditScoreRepository.findByUserId(userKycDto.getUserId())
						.orElseThrow(() -> new UsernameNotFoundException("CreditScore Not Found With This User_id: " + userKycDto.getUserId()));
				int signum = Double.compare(creditScore.getTotalExposure(), 0);

				if (signum > 0) {

					if (userKycDto.getLoanEligibility() != 0) {
						UserLoanEligibility userLoanEligibility;

						User user = userRepository.findByid(userKycDto.getUserId())
								.orElseThrow(() -> new UsernameNotFoundException("User not found with this id: " + userKycDto.getUserId()));
						LoanEligibility loanEligibility = loanEligibilityRepository.findById(userKycDto.getLoanEligibility())
								.orElseThrow(() -> new UsernameNotFoundException("LoanEligibility not found with this id: " + userKycDto.getLoanEligibility()));

						Optional<UserLoanEligibility> loanEligibilityOptional = userLoanEligibilityRepository.getByUserId(user.getId());
						long oldEligibilityAmount = 0;
						long newEligibilityAmount = 0;

						if (loanEligibilityOptional.isPresent()) {
							userLoanEligibility = loanEligibilityOptional.get();
							oldEligibilityAmount = userLoanEligibility.getEligibilityAmount();
						} else {
							userLoanEligibility = new UserLoanEligibility();

						}
						userLoanEligibility.setEligibility(loanEligibility);
						userLoanEligibility.setUser(user);

						newEligibilityAmount = calculateEligibilityBasedOnExposer(loanEligibility.getEndAmount(), creditScore.getTotalExposure());

						if (newEligibilityAmount != oldEligibilityAmount) {
							userLoanEligibility.setOldEligibilityAmount(oldEligibilityAmount);
							updateEligibilityAmount = true;
						}

						userLoanEligibility.setEligibilityAmount(calculateEligibilityBasedOnExposer(loanEligibility.getEndAmount(), creditScore.getTotalExposure()));
						userLoanEligibility = userLoanEligibilityRepository.save(userLoanEligibility);


						UtilizeUserCredit userCredit = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(user.getId());

						if (userCredit == null) {
							userCredit = new UtilizeUserCredit();
							userCredit.setUserLoanEligibility(userLoanEligibility);
							userCredit.setAvailableBalance((double) calculateEligibilityBasedOnExposer(loanEligibility.getEndAmount() ,  creditScore.getTotalExposure()));
							userCredit.setUtilizeBalance(0.0);
							userCredit.setUser(user);
							utilizeUserCreditRepository.save(userCredit);
						} else if (updateEligibilityAmount) {
							long increaseAmount = calculateEligibilityBasedOnExposer(loanEligibility.getEndAmount() , creditScore.getTotalExposure()) - oldEligibilityAmount;
							double availableAmount = userCredit.getAvailableBalance();
							userCredit.setAvailableBalance(availableAmount + increaseAmount);
							utilizeUserCreditRepository.save(userCredit);
						}

					}
					message = "KYC UPDATED SUCCESSFULLY.";
				}else{
					message = "KYC UPDATED SUCCESSFULLY. SORRY, THIS USER ARE NOT ELIGIBLE FOR LOAN.";
				}

				SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("Success")
						.message(message).build();
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

	private Long calculateEligibilityBasedOnExposer(Long levelAmount, Float exposure) {
		double multiplyAmount = levelAmount * exposure;
		double calculation = multiplyAmount / 100;
		return (long)  calculation;

	}

	@Override
	@Transactional
	public ResponseEntity<?> enableDisEnabledUser(ApprovalDeclineDto dto) {
		Optional<User> optionalUser = userService.getUserById(dto.getId());
		Optional<KYC> optionalKyc = kycRepository.findByUserId(dto.getId());

		if (optionalUser.isEmpty()) {
			SuccessDto errorResponse = SuccessDto.builder()
					.code(HttpStatus.NOT_FOUND.value())
					.status("Error")
					.message("User not found")
					.build();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
		if (optionalKyc.isEmpty()) {
			SuccessDto errorResponse = SuccessDto.builder()
					.code(HttpStatus.NOT_FOUND.value())
					.status("Error")
					.message("User KYC Record Not Found.")
					.build();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		User user = optionalUser.get();
		KYC kyc = optionalKyc.get();

		if (dto.isApprove()) {
			kyc.setStatus(String.valueOf(KycStatus.APPROVED));
			user.setIsApproved(true);
		} else {
			kyc.setStatus(String.valueOf(KycStatus.DECLINED));
			kyc.setReason(dto.getReason());
			user.setIsApproved(false);
		}
		kycRepository.save(kyc);
		userRepository.save(user);

		String status = dto.isApprove() ? "Enabled" : "Disabled";
		SuccessDto successResponse = SuccessDto.builder()
				.code(HttpStatus.OK.value())
				.status("Success")
				.message("User with ID " + dto.getId() + " has been " + status)
				.build();
		return ResponseEntity.status(HttpStatus.OK).body(successResponse);
	}

	@Override
	@Transactional

	public ResponseEntity<?> approvedUser() {

			List<User> userApproved = userRepository.findByIsApproved(true);
			System.out.println("Size ::: " + userApproved.size());
		List<UserDto> userDtos = getUserDtos(userApproved , true);

		SuccessDto successResponse = SuccessDto.builder()
					.code(HttpStatus.OK.value())
					.status("Success")
					.message("ACTIVE USERS")
					.data(userDtos)
					.build();
			return ResponseEntity.status(HttpStatus.OK).body(successResponse);
//		} catch (Exception ex) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching user status.");
//		}


	}

	private List<UserDto> getUserDtos(List<User> userApproved , boolean approve) {
		List<UserDto> userDtos = userApproved.stream().map(user -> {
			int userId = user.getId();
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			String mobile = user.getPhoneNumber();
			String email = user.getEmail();


			String status = "";

			Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(userId);
			int score = optionalCreditScore.map(CreditScore::getTotalCreditScore).orElse(0);


				Optional<KYC> optionalKyc = kycRepository.findByUserId(userId);
				if (optionalKyc.isPresent()) {
					status = optionalKyc.get().getStatus();
				}
				return new UserDto(userId, firstName, lastName, mobile, email, score, status);

		}).collect(Collectors.toList());
		return userDtos;
	}

	public ResponseEntity<?> updateUser(UserDtoForUpdate userDtoForUpdate, int id) {
		try {
			Optional<User> optionalUser = userRepository.findById(id);

			if (optionalUser.isPresent()) {
				User user = optionalUser.get();
				user.setFirstName(userDtoForUpdate.getFirstName());
				user.setLastName(userDtoForUpdate.getLastName());
				user.setPhoneNumber(userDtoForUpdate.getPhoneNumber());
				user.setEmail(userDtoForUpdate.getEmail());
				userRepository.save(user);

				SuccessDto successResponse = SuccessDto.builder()
						.code(HttpStatus.OK.value())
						.status("Success")
						.message("User updated successfully")
						.build();
				return ResponseEntity.ok(successResponse);
			} else {
				// Return an error response using ErrorDto
				ErrorDto errorResponse = ErrorDto.builder()
						.code(HttpStatus.NOT_FOUND.value())
						.status("Error")
						.message("User not found")
						.build();
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL SERVER ERROR");
		}
	}




	public ResponseEntity<?> getUserById(int id) {
		try {
			Optional<User> optionalUser = userRepository.findById(id);

			if (optionalUser.isPresent()) {
				User user = optionalUser.get();
				UserDtoForUpdate userDto = new UserDtoForUpdate();
				userDto.setFirstName(user.getFirstName());
				userDto.setLastName(user.getLastName());
				userDto.setPhoneNumber(user.getPhoneNumber());
				userDto.setEmail(user.getEmail());

				SuccessDto successResponse = SuccessDto.builder()
						.code(HttpStatus.OK.value())
						.status("Success")
						.message("User Deatials ")
						.data(userDto)
						.build();
				return ResponseEntity.ok(successResponse);
			} else {
				ErrorDto errorResponse = ErrorDto.builder()
						.code(HttpStatus.NOT_FOUND.value())
						.status("Error")
						.message("User not found")
						.build();
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL SERVER ERROR");
		}
	}


	@Override
	@Transactional
	public ResponseEntity<?> deleteUser(int userId) {
		try {
			if (disbursementHistoryRepository.existsByUserIdAndCollectionCompletedTrue(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("User cannot be deleted because they have an ongoing loan.");
			}
			kycRepository.deleteByUserId(userId);
			disbursementHistoryRepository.deleteByUserId(userId);
			utilizeUserCreditRepository.deleteByUserId(userId);
			userRepository.deleteById(userId);
			return ResponseEntity.ok("User deleted successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to delete user. Reason: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> getWalletBalance() throws JsonProcessingException {
		String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements/balance";
		HttpHeaders headers = appCommon.getHttpHeaders();
		String network = "MTN";

		try {
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);

			URI uri = UriComponentsBuilder.fromUriString(apiUrl)
					.queryParam("network", network)
					.build()
					.toUri();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
			HttpStatusCode statusCode = responseEntity.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				String responseBody = responseEntity.getBody();
				SuccessDto successDto = SuccessDto.builder()
						.message("Wallet Balance retrieved successfully")
						.code(statusCode.value())
						.status(statusCode.toString())
						.data(responseBody)
						.build();
				return ResponseEntity.status(statusCode).body(successDto);
			} else {
				return ResponseEntity.status(statusCode).build();
			}
		} catch (RestClientException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve wallet balance: " + ex.getMessage());
		}
	}


	@Override
	public ResponseEntity<?> getWalletCollections() throws JsonProcessingException {
		String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections/balance";
		HttpHeaders headers = appCommon.getHttpHeaders();

		try {

			HttpEntity<String> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, String.class);
			HttpStatusCode statusCode = responseEntity.getStatusCode();

			if (statusCode.is2xxSuccessful()) {
				String responseBody = responseEntity.getBody();
				SuccessDto successDto = SuccessDto.builder()
						.message("Collections retrieved successfully")
						.code(statusCode.value())
						.status(statusCode.toString())
						.data(responseBody)
						.build();

				return ResponseEntity.status(statusCode).body(successDto);
			} else {
				return ResponseEntity.status(statusCode).build();
			}
		} catch (RestClientException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve wallet collections:" + ex.getMessage());
		}
	}
}