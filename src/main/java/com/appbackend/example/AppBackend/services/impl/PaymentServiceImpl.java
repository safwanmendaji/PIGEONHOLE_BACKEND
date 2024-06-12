package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.DisbursementsType;
import com.appbackend.example.AppBackend.enums.InterestFrequency;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.*;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
//    @Autowired
    private  AppCommon appCommon;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.username}")
    private String username;

//    @Autowired
//    private


    @Value("${payment.password}")
    private String password;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Autowired
    private UtilizeUserCreditRepository utilizeUserCreditRepository;


    @Autowired
    private StorageService storageService;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private DisbursementInterestCountRepository disbursementInterestCountRepository;


    @Autowired
    private CollectionAmountCalculationRepository collectionAmountCalculationRepository;

    @Autowired
    private MonthlyCollectionInfoRepository monthlyCollectionInfoRepository;


    public PaymentServiceImpl(){

    }
    @Autowired
    PaymentServiceImpl(AppCommon appCommon){
        this.appCommon = appCommon;
    }

    @Override
    public ResponseEntity<?> payment(PaymentDto paymentDto) {
        logger.info("Call payment method with request :: " + paymentDto);
        try {
            logger.info("find user with userId : " + paymentDto.getUserId());
            Optional<User> optionalUser = userRepository.findById(paymentDto.getUserId());
            logger.info("User Found ::: " + optionalUser.isPresent());
            if (!optionalUser.isPresent()) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.NOT_FOUND.value())
                        .status("ERROR")
                        .message("USER NOT FOUND FOR THIS USERID: " + paymentDto.getUserId())
                        .build();
                logger.error("User not found :: " + paymentDto.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
            }
            logger.info("check the UtilizeUserCredit ");
            UtilizeUserCredit userCredit = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(paymentDto.getUserId());
            if (userCredit == null) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("ERROR")
                        .message("DON'T HAVE ANY LOAN ELIGIBILITY AMOUNT IN YOUR ACCOUNT. PLEASE CONTACT TO ADMINISTRATOR.")
                        .build();
                logger.info("UserCredit not found::: ");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            }
            logger.info("UserCredit found::: ");

            double availableLoanAmount = userCredit.getAvailableBalance();

            if (availableLoanAmount < paymentDto.getAmount()) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("ERROR")
                        .message("INSUFFICIENT BALANCE IN YOUR ACCOUNT. AVAILABLE BALANCE IS: " + availableLoanAmount)
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = appCommon.getHttpHeaders();
            headers.set("Content-Type", "application/json");
            String url = "https://api.valueadditionmicrofinance.com/v1/disbursements/balance?network=MTN";

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
//                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//                String responseBody = responseEntity.getBody();
//
//
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode root = mapper.readTree(responseBody);
//
//                JsonNode balanceNode = root.path("balance");
//
//                double availableBalance = balanceNode.path("availableBalance").asDouble();
//
//                logger.info("Balance in EXTERNAL SOURCE------>"+availableBalance);
//
//
//                if (paymentDto.getAmount() > availableBalance) {
//                    ErrorDto errorDto = ErrorDto.builder()
//                            .code(HttpStatus.BAD_REQUEST.value())
//                            .status("ERROR")
//                            .message("INSUFFICIENT BALANCE FROM THE EXTERNAL SOURCE")
//                            .build();
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
//                }

            } catch (HttpClientErrorException e) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("ERROR")
                        .message("Error fetching balance: " + e.getStatusCode())
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            } catch (Exception e) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .status("ERROR")
                        .message("Error processing balance check: " + e.getMessage())
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
            }

            User user = optionalUser.get();
            UUID uuid = UUID.randomUUID();
            paymentDto.setReference(uuid);

            DisbursementsHistory disbursementsHistory = buildDisbursementsHistory(paymentDto, user, DisbursementsStatus.INITIALIZE, new DisbursementsHistory());

            disbursementsHistory = processDisbursements(paymentDto, user, userCredit, disbursementsHistory);

            SuccessDto successDto = SuccessDto.builder()
                    .message("DISBURSEMENTS TRANSACTION STATUS IS: " + disbursementsHistory.getPaymentStatus())
                    .code(HttpStatus.OK.value())
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(successDto);

        } catch (Exception ex) {
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status("ERROR")
                    .message(ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        }
    }

    private DisbursementsHistory processDisbursements(PaymentDto paymentDto, User user, UtilizeUserCredit userCredit , DisbursementsHistory disbursementsHistory) throws JsonProcessingException {
        logger.info("Call processDisbursements method :");
        String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements";
        HttpHeaders headers = appCommon.getHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Ensure the content type is set

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(buildDisbursementsReq(paymentDto, user)); // Convert request body to JSON string
        logger.info("DisbursementsReq ::  " + requestJson);

        disbursementsHistory.setDisbursementsRequest(requestJson);

        HttpEntity<String> apiRequestEntity = new HttpEntity<>(requestJson, headers);

        disbursementsRepository.save(disbursementsHistory);

        logger.info("Call API with apiRequestEntity ::  " + apiRequestEntity);

        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);
        String responseBody = responseEntity.getBody();
        Map<?  , ?> map = objectMapper.readValue(responseBody, Map.class);

        logger.info("ResponseEntity ::: = > " + responseEntity);
        logger.info("ResponseBody ::: = > " + responseEntity.getBody());

        String refId = (String) map.get("transactionReference");
        disbursementsHistory.setDisbursementsResponse(responseBody);
        disbursementsHistory.setDisbursementsTransactionId(refId);

        String status = checkDisbursementsCheckStatus(disbursementsHistory.getDisbursementsTransactionId() , headers);
        if(status.equals(DisbursementsStatus.INITIALIZE.name()) && (Integer) map.get("code") >= 200 && (Integer) map.get("code") <= 300)
            status = DisbursementsStatus.PENDING.name();
        disbursementsHistory.setPaymentStatus(status);

        disbursementsHistory = disbursementsRepository.save(disbursementsHistory);

        calculateUtilization(paymentDto, userCredit, disbursementsHistory, status);
        return disbursementsHistory;
    }


    private void calculateUtilization(PaymentDto paymentDto, UtilizeUserCredit userUtilize, DisbursementsHistory disbursementsHistory, String status) {
        if(status.equals(DisbursementsStatus.SUCCEEDED.name())){
            double availableBalance = userUtilize.getAvailableBalance();
            UtilizeUserCredit userUtilizeCredit = new UtilizeUserCredit();
            userUtilizeCredit.setUtilizeBalance((double) paymentDto.getAmount());
            userUtilizeCredit.setAvailableBalance(availableBalance - paymentDto.getAmount());
            userUtilizeCredit.setHistory(disbursementsHistory);
            userUtilizeCredit.setUserLoanEligibility(userUtilize.getUserLoanEligibility());
            userUtilizeCredit.setUser(userUtilize.getUser());
            userUtilizeCredit.setUtilizeOn(disbursementsHistory.getCreatedOn());
            utilizeUserCreditRepository.save(userUtilizeCredit);
        }
    }

    @Override
    public ResponseEntity<?> checkDisbursementsStatus(String transactionId , HttpHeaders headers){
        String status = checkDisbursementsCheckStatus(transactionId , null);

        SuccessDto successDto = SuccessDto.builder().message("DISBURSEMENTS TRANSACTION STATUS IS : .").code(HttpStatus.OK.value()).status("SUCCESS").data(status).build();

        return ResponseEntity.status(HttpStatus.OK).body(successDto);
    }

    @Override
    public ResponseEntity<?> getByDisbursementHistoryById(int id) {

        Optional<DisbursementsHistory> response = disbursementsRepository.findById(id);

        if (response.isPresent()) {
            DisbursementHistoryDTO dto = buildDisbursementDto(response.get() , true);
            SuccessDto successDto = SuccessDto.builder()
                    .message("DISBURSEMENTS TRANSACTION STATUS IS : .")
                    .code(HttpStatus.OK.value())
                    .status("SUCCESS")
                    .data(dto)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data not found for ID: " + id);
        }
    }

    @Override
    public ResponseEntity<?> getAllDisbursementHistoryGroupedByType() {
        List<DisbursementsHistory> allDisbursements = disbursementsRepository.findAll();

        Map<String, List<DisbursementsHistory>> groupedData = allDisbursements.stream()
                .collect(Collectors.groupingBy(DisbursementsHistory::getDisbursementsType));


        Map<String, List<DisbursementHistoryDTO>> groupedDtoData = groupedData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        entry.getValue().stream()
                                .sorted((d1, d2) -> Long.compare(d2.getId(), d1.getId())) // Sort by ID in descending order
                                .map(disbursement -> {
                                    return buildDisbursementDto(disbursement ,  false);
                                })
                                .collect(Collectors.toList())));

        SuccessDto successDto = SuccessDto.builder()
                .message("Disbursement transaction status")
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .data(groupedDtoData)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(successDto);
    }

    private DisbursementHistoryDTO buildDisbursementDto(DisbursementsHistory disbursement , boolean needInterestInfo) {
        DisbursementHistoryDTO dto = new DisbursementHistoryDTO();
        dto.setId(disbursement.getId());
        dto.setUserId(disbursement.getUserId());
        dto.setDisbursementsType(disbursement.getDisbursementsType());
        dto.setAmount(disbursement.getAmount());
        dto.setDisbursementsTransactionId(disbursement.getDisbursementsTransactionId());
        dto.setDisbursementsResponse(disbursement.getDisbursementsResponse());
        dto.setPaymentStatus(disbursement.getPaymentStatus());
        dto.setStudentCode(disbursement.getStudentCode());
        dto.setStudentName(disbursement.getStudentName());
        dto.setSchoolName(disbursement.getSchoolName());
        dto.setStudentClass(disbursement.getStudentClass());
        dto.setOutstandingFees(disbursement.getOutstandingFees());
        dto.setTeamLeadName(disbursement.getTeamLeadName());
        dto.setTeamLeadContactNumber(disbursement.getTeamLeadContactNumber());
        dto.setDisbursementsRequest(disbursement.getDisbursementsRequest());
        dto.setStartDate(disbursement.getStartDateForTravel());
        dto.setEndDate(disbursement.getEndDateForTravel());
        dto.setDestination(disbursement.getDestination());
        dto.setReferenceId(disbursement.getReferenceId());
        dto.setNarration(disbursement.getNarration());
//        dto.setApprovedForTravel(disbursement.getApprovedForTravel());
        dto.setDocument(disbursement.getDocument());
        dto.setTravelDeclineReason(disbursement.getTravelDeclineReason());
        dto.setReason(disbursement.getReason());
        dto.setDisbursementFailReason(disbursement.getDisbursementFailReason());
        dto.setApprovedBy(disbursement.getApprovedBy());
        dto.setApprovedOn(disbursement.getApprovedOn());
        dto.setUpdateBy(disbursement.getUpdateBy());
        dto.setUpdateOn(disbursement.getUpdateOn());
        dto.setCreatedBy(disbursement.getCreatedBy());
        dto.setCreatedOn(disbursement.getCreatedOn());
        dto.setDisbursementEndDate(disbursement.getDisbursementEndDate());
        dto.setDaysInArray(disbursement.getDaysInArrears() != null ? disbursement.getDaysInArrears() : false);

        User user = userRepository.findById(disbursement.getUserId()).orElse(null);
        if (user != null) {
            dto.setUsername(user.getFirstName());
            dto.setMobile(user.getPhoneNumber());
            dto.setEmail(user.getEmail());
        }


        if(needInterestInfo){
            try {
                DisbursementInterestDto interestDto = new DisbursementInterestDto();
                Optional<CollectionAmountCalculation> collectionAmountCalculationOptional = collectionAmountCalculationRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursement.getId());
                DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.findFistByDisbursementsHistoryIdOrderByIdDesc(disbursement.getId());
                MonthlyCollectionInfo monthlyCollectionInfo = monthlyCollectionInfoRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursement.getId());
                CollectionAmountCalculation collectionAmountCalculation = collectionAmountCalculationOptional.get();



                interestDto.setDisbursementAmount((long) disbursement.getAmount());
                interestDto.setAmountToPay(collectionAmountCalculation.getRemainingPayment());
                interestDto.setMinimumAmountToPay(monthlyCollectionInfo.getMinimumAmount());
                interestDto.setLastInterestCountDate(disbursementInterestCount.getInterestCalculationDate());
                interestDto.setNextInterestCountDate(disbursementInterestCount.getInterestCalculationDate().plusWeeks(1));
                interestDto.setLastPaymentDate(collectionAmountCalculation.getLastPaymentDate());
                interestDto.setLastPaidAmount(collectionAmountCalculation.getPayAmount());

                dto.setDisbursementInterestInfo(interestDto);
            }catch (Exception e){
                e.printStackTrace();
            }

            logger.info("Find the Collection History :: ");
            List<CollectionHistory> collectionHistoryList = collectionHistoryRepository.findByDisbursementsHistoryIdOrderByIdDesc(disbursement.getId());
            logger.info("Collection History List Size  ::: " + collectionHistoryList.size());


            List<CollectionHistoryDTO> collectionHistoryDTOList = collectionHistoryList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            dto.setCollectionHistoryDTOList(collectionHistoryDTOList);

        }

        return dto;
    }

    public  CollectionHistoryDTO convertToDto(CollectionHistory collectionHistory) {
        CollectionHistoryDTO dto = new CollectionHistoryDTO();
        dto.setId(collectionHistory.getId());
        dto.setDisbursementsHistoryId(collectionHistory.getDisbursementsHistory() != null ? collectionHistory.getDisbursementsHistory().getId() : null);
        dto.setResponseTransactionId(collectionHistory.getResponseTransactionId());
        dto.setRequestTransactionId(collectionHistory.getRequestTransactionId());
        dto.setPaymentAmount(collectionHistory.getPaymentAmount());
        dto.setPaymentDate(collectionHistory.getPaymentDate());
        dto.setUserId(collectionHistory.getUser() != null ? collectionHistory.getUser().getId() : null);
        dto.setCollectionRequest(collectionHistory.getCollectionRequest());
        dto.setStatus(collectionHistory.getStatus());
        dto.setResponse(collectionHistory.getResponse());
        return dto;
    }

    @Override
    public ResponseEntity<?> getApprovedForTravel(ApprovalDeclineDto dto) throws JsonProcessingException {
        try {}catch (Exception e){
            ErrorDto errorDto = ErrorDto.builder()
                    .message("Some thing when wrong. " + e.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("ERROR")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);

        }
        return null;
    }

    @Override
    public ResponseEntity<?> getDisbursementHistoryOfUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        List<DisbursementsHistory> disbursementHistoryOfUser = disbursementsRepository.findByUserId(userId);
        Map<String, List<DisbursementsHistory>> groupedDisbursementHistory = disbursementHistoryOfUser.stream()
                .collect(Collectors.groupingBy(DisbursementsHistory::getDisbursementsType));
        String message = user.getFirstName() + " Disbursement History";
        SuccessDto successDto = SuccessDto.builder()
                .message(message)
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .data(groupedDisbursementHistory)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(successDto);
    }

    @Override
    public ResponseEntity<?> getDisbursementStatus(String id) {

        String status = checkDisbursementsCheckStatus(id , null);

        SuccessDto successDto = SuccessDto.builder()
                .message("message")
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .data(status)
                .build();
        return  ResponseEntity.status(HttpStatus.OK).body(successDto);
    }

    @Override
    public ResponseEntity<?> uploadDisbursementDocument(MultipartFile file, int disbursementId) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        Optional<DisbursementsHistory> disbursementOptional = disbursementsRepository.findById(disbursementId);
        if (!disbursementOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Disbursement ID not found");
        }

        DisbursementsHistory disbursement = disbursementOptional.get();
        if (!disbursement.getPaymentStatus().equalsIgnoreCase("SUCCEEDED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment status is not succeeded");
        }

        try {
            String s3Url = storageService.uploadFileToS3(file);

            disbursement.setDocument(s3Url);
            disbursementsRepository.save(disbursement);

            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully: " + s3Url);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }



    public void checkDisbursementStatusAndUpdate(DisbursementsHistory disbursementsHistory){
        String status = checkDisbursementsCheckStatus(disbursementsHistory.getDisbursementsTransactionId() , null);
        disbursementsHistory.setPaymentStatus(status);
        if(status.equals(DisbursementsStatus.SUCCEEDED.name())){
            disbursementsHistory.setCollectionCompleted(false);
            PaymentDto paymentDto  = new PaymentDto();
            paymentDto.setAmount(disbursementsHistory.getAmount());

            //Utilize the user credit
            UtilizeUserCredit userCreditUtilize = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(disbursementsHistory.getUserId());
            calculateUtilization(paymentDto , userCreditUtilize , disbursementsHistory , status);

            // Add Record for interest count for 1st week
            InterestCountMaster interestCountMaster = interestRepository.findFirstByOrderById().get();
            DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.save(buildDisbursementInterestCount(interestCountMaster, disbursementsHistory, null , null));


            //Add Monthly amount calculation record
            buildAndSaveMonthlyCollectionInfo(disbursementsHistory ,null);



            //Add Collection_Amount_Count Record
            buildAndSaveCollectionAmount(disbursementInterestCount);

        }
        disbursementsRepository.save(disbursementsHistory);
    }



    public String checkDisbursementsCheckStatus(String transactionId , HttpHeaders headers) {
        try {
            logger.info("header ::: " + headers);
            if(headers == null){
                headers = appCommon.getHttpHeaders();
            }
            String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements/" + transactionId;
            HttpEntity<Map> apiRequestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, apiRequestEntity, String.class);
            String responseBody = responseEntity.getBody();

            // Parse JSON string to Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            // Get the nested map under the "data" key
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");

            System.out.println("Result ==>>> " + dataMap);
            // Get the value of "transactionStatus" from the nested map
            String transactionStatus = (String) dataMap.get("transactionStatus");


            return transactionStatus;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DisbursementsHistory buildDisbursementsHistory(PaymentDto paymentDto , User user , DisbursementsStatus disbursementsStatus , DisbursementsHistory disbursementsHistory) {
        disbursementsHistory = new DisbursementsHistory();
        LocalDate currentDate = LocalDate.now();
        logger.info("Call buildDisbursementsHistory method:: ");
        try{
            if(paymentDto.getDisbursementsType().name().equals(DisbursementsType.TRAVEL_LOAN.name())){
                disbursementsHistory.setTeamLeadName(paymentDto.getTravelDetails().getTeamLeadName());
                disbursementsHistory.setTeamLeadContactNumber(paymentDto.getTravelDetails().getTeamLeadContactNumber());
                disbursementsHistory.setStartDateForTravel(paymentDto.getTravelDetails().getStartDate());
                disbursementsHistory.setEndDateForTravel(paymentDto.getTravelDetails().getEndDate());
                disbursementsHistory.setDestination(paymentDto.getTravelDetails().getDestination());
                disbursementsHistory.setDocument(paymentDto.getDocument());
                disbursementsHistory.setAmount(paymentDto.getAmount());

            }else if( paymentDto.getDisbursementsType().name().equals(DisbursementsType.SCHOOL_FEES.name()) ){
                disbursementsHistory.setStudentCode(paymentDto.getStudentDetails().getStudentCode());
                disbursementsHistory.setSchoolName(paymentDto.getStudentDetails().getSchoolName());
                disbursementsHistory.setStudentName(paymentDto.getStudentDetails().getName());
                disbursementsHistory.setOutstandingFees((double) paymentDto.getStudentDetails().getOutstandingFees());
                disbursementsHistory.setStudentClass(paymentDto.getStudentDetails().getStudentClass());
                if(paymentDto.getDocument() != null)
                    disbursementsHistory.setDocument(paymentDto.getDocument());
                disbursementsHistory.setAmount(paymentDto.getStudentDetails().getOutstandingFees());
            }else{
                disbursementsHistory.setReason(paymentDto.getReason());
                disbursementsHistory.setAmount(paymentDto.getAmount());
            }

            disbursementsHistory.setUserId(user.getId());
            disbursementsHistory.setCreatedOn(LocalDateTime.now());
            disbursementsHistory.setPaymentStatus(DisbursementsStatus.INITIALIZE.name());
            disbursementsHistory.setDisbursementsType(paymentDto.getDisbursementsType().name());
            disbursementsHistory.setNarration(paymentDto.getDisbursementsType().name());
            disbursementsHistory.setReferenceId(paymentDto.getReference());
            disbursementsHistory.setDisbursementDuration(paymentDto.getNumberOfDurationMonth());

            LocalDate endDate = currentDate.plusMonths(paymentDto.getNumberOfDurationMonth());
            disbursementsHistory.setDisbursementEndDate(endDate);
            disbursementsHistory.setDaysInArrears(false);


//            disbursementsHistory = disbursementsRepository.save(disbursementsHistory);

        }catch (Exception e){
            e.printStackTrace();
        }
        return disbursementsHistory;
    }

    private Map<String, Object> buildDisbursementsReq(PaymentDto paymentDto , User user){
        Map<String, Object> paymentMap = new HashMap<>();

        try {
            String account = user.getPhoneNumber();
            account = account.replace("+256", "");
            System.out.println("account==>>> " + account);
            paymentMap.put("type", "mm");
            paymentMap.put("account", account);
            paymentMap.put("amount", paymentDto.getAmount());
            paymentMap.put("narration", paymentDto.getDisbursementsType().name());
            paymentMap.put("reference", paymentDto.getReference());

//
//            // Convert the map to a JsonObject
//            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
//            for (Map.Entry<String, Object> entry : paymentMap.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//
//                if (value instanceof String) {
//                    jsonObjectBuilder.add(key, (String) value);
//                }  else if (value instanceof Float) {
//                    jsonObjectBuilder.add(key, (Float) value);
//                }else if (value instanceof UUID) {
//                    jsonObjectBuilder.add(key,  value.toString());
//                }
//            }

            // Build the JsonObject
//            JsonObject jsonObject = jsonObjectBuilder.build();
//            logger.info("Json ===>>> " + jsonObject);

            return paymentMap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
    public ResponseEntity<?> test() {
//        updateDisbursementStatusAndUtilization();
        return null;
    }


    private void disbursementsInterestCount(){
        Optional<InterestCountMaster> interestCountMasterOptional = interestRepository.findFirstByOrderById();
        if(interestCountMasterOptional.isPresent()) {
            InterestCountMaster interestCountMaster = interestCountMasterOptional.get();
            List<DisbursementsHistory> disbursementsHistoryList =  disbursementsRepository.findByPaymentStatusAndCollectionCompleted(DisbursementsStatus.SUCCEEDED.name() , false);
            disbursementsHistoryList.forEach(disbursementsHistory -> {
                DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.findFistByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId());

                Optional<CollectionAmountCalculation> collectionAmountCalculationOptional = collectionAmountCalculationRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId());
                CollectionAmountCalculation collectionAmountCalculation = collectionAmountCalculationOptional.orElse(null);

                if(disbursementInterestCount != null){
                    long interestCountDays = ChronoUnit.DAYS.between(disbursementInterestCount.getInterestCalculationDate(), LocalDate.now());
                    if(interestCountMaster.getInterestFrequency().equals(InterestFrequency.WEEK.name())){
                        disbursementInterestCountRepository.save(Objects.requireNonNull(weeklyInterestCount(interestCountMaster, disbursementsHistory, interestCountDays, collectionAmountCalculation, disbursementInterestCount)));
                    }
                }else {
                    disbursementInterestCountRepository.save(buildDisbursementInterestCount(interestCountMaster, disbursementsHistory, collectionAmountCalculation , null));
                }
            });
        }
    }

    private static DisbursementInterestCount weeklyInterestCount(InterestCountMaster interestCountMaster, DisbursementsHistory disbursementsHistory, long interestCountDays, CollectionAmountCalculation collectionAmountCalculation, DisbursementInterestCount disbursementInterestCount) {
        double interestCountAmount = 0;

        if(interestCountDays > 7){
            return buildDisbursementInterestCount(interestCountMaster, disbursementsHistory, collectionAmountCalculation, disbursementInterestCount);
        }
        return null;

    }


    private static DisbursementInterestCount buildDisbursementInterestCount(InterestCountMaster interestCountMaster, DisbursementsHistory disbursementsHistory, CollectionAmountCalculation collectionAmountCalculation, DisbursementInterestCount disbursementInterestCount) {
        double interestCountAmount;
        DisbursementInterestCount   disbursementInterestCountNew = new DisbursementInterestCount();


        disbursementInterestCountNew.setDisbursementsHistory(disbursementsHistory);
        disbursementInterestCountNew.setInterestCountMaster(interestCountMaster);
        disbursementInterestCountNew.setUserId(disbursementsHistory.getUserId());

        if(collectionAmountCalculation != null){
            interestCountAmount = collectionAmountCalculation.getRemainingPayment();
        }else{
            interestCountAmount = disbursementInterestCount != null ? disbursementInterestCount.getEndingBalance() : disbursementsHistory.getAmount();
        }

        double interestAmountMultiple = interestCountAmount * interestCountMaster.getInterestRate();
        double finalInterestAmount = interestAmountMultiple / 100;


        disbursementInterestCountNew.setInterestAmount((long) finalInterestAmount);
        disbursementInterestCountNew.setBeginningBalance((long) interestCountAmount);
        disbursementInterestCountNew.setEndingBalance((long) (finalInterestAmount + interestCountAmount));
        disbursementInterestCountNew.setInterestCalculationDate(LocalDate.now());
        return disbursementInterestCountNew;
    }


    private void buildAndSaveMonthlyCollectionInfo(DisbursementsHistory disbursementsHistory , MonthlyCollectionInfo monthlyCollectionInfoOld){
        MonthlyCollectionInfo monthlyCollectionInfo = new MonthlyCollectionInfo();
        InterestCountMaster interestCountMaster = interestRepository.findFirstByOrderById().get();
        monthlyCollectionInfo.setDisbursementsHistory(disbursementsHistory);

        if(monthlyCollectionInfoOld !=null){
            LocalDate startDate = monthlyCollectionInfoOld.getMonthEndDate().plusDays(1);
            monthlyCollectionInfo.setMinimumAmount(monthlyCollectionInfoOld.getMinimumAmount());
            monthlyCollectionInfo.setMonthStartDate(startDate);
            monthlyCollectionInfo.setMonthEndDate(startDate.plusMonths(1));
            monthlyCollectionInfo.setTotalPayAmountInMonth(0.0);
            if(disbursementsHistory.getDisbursementEndDate().isEqual(startDate.plusMonths(1))){
                monthlyCollectionInfo.setLastMonth(true);
            }
        }else {
            monthlyCollectionInfo.setDisbursementsHistory(disbursementsHistory);
            monthlyCollectionInfo.setMinimumAmount(calculateLoanPayment(interestCountMaster.getInterestRate(), disbursementsHistory.getDisbursementDuration(), disbursementsHistory.getAmount()));
            monthlyCollectionInfo.setMonthStartDate(disbursementsHistory.getCreatedOn().toLocalDate());
            monthlyCollectionInfo.setMonthEndDate(disbursementsHistory.getCreatedOn().toLocalDate().plusMonths(1));
            monthlyCollectionInfo.setTotalPayAmountInMonth(0.0);
        }
        monthlyCollectionInfoRepository.save(monthlyCollectionInfo);
    }


        public static double calculateLoanPayment(double interestRate, int numberOfPeriods, double loanAmount) {


            // Calculate the rate per period
            double ratePerPeriod = interestRate / 100 * 4;

            // Calculate the PMT
            double pmt;
            if (ratePerPeriod == 0) {
                pmt = -(loanAmount / numberOfPeriods);
            } else {
                double factor = Math.pow(1 + ratePerPeriod, numberOfPeriods);
                pmt = -(loanAmount * ratePerPeriod * factor / (factor - 1));
            }

            // Check if pmt is infinite
            if (Double.isInfinite(pmt)) {
                pmt = 0; // or handle it in another way as needed
            }

            // Round up to the nearest thousand
            double roundedUp = Math.ceil(pmt / 1000) * 1000;

            // Multiply by -1 to change the sign and add 1000
            double finalAmount = (roundedUp * -1) + 1000;

            // Use BigDecimal to round to 2 decimal places
            BigDecimal bd = new BigDecimal(finalAmount);
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            return bd.doubleValue();
    }


    private void buildAndSaveCollectionAmount(DisbursementInterestCount disbursementInterestCount){
        CollectionAmountCalculation collectionAmountCalculation = new CollectionAmountCalculation();
        collectionAmountCalculation.setDisbursementsHistory(disbursementInterestCount.getDisbursementsHistory());
        collectionAmountCalculation.setRemainingPayment((double) disbursementInterestCount.getEndingBalance());
        collectionAmountCalculation.setTotalPayAmount(0.0);
        collectionAmountCalculation.setPayAmount(0.0);
        collectionAmountCalculation.setUserId(disbursementInterestCount.getUserId());

        collectionAmountCalculationRepository.save(collectionAmountCalculation);

    }




}
