package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.entities.UtilizeUserCredit;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.DisbursementsType;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.repositories.UtilizeUserCreditRepository;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private AppCommon appCommon;
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



    @Override
    public ResponseEntity<?> payment(PaymentDto paymentDto) {
        try {
            Optional<User> optionalUser = userRepository.findById(paymentDto.getUserId());

            if (!optionalUser.isPresent()) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.NOT_FOUND.value())
                        .status("ERROR")
                        .message("USER NOT FOUND FOR THIS USERID: " + paymentDto.getUserId())
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
            }

            UtilizeUserCredit userCredit = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(paymentDto.getUserId());

            if (userCredit == null) {
                ErrorDto errorDto = ErrorDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("ERROR")
                        .message("DON'T HAVE ANY LOAN ELIGIBILITY AMOUNT IN YOUR ACCOUNT. PLEASE CONTACT TO ADMINISTRATOR.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            }

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
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                String responseBody = responseEntity.getBody();


                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody);

                JsonNode balanceNode = root.path("balance");

                double availableBalance = balanceNode.path("availableBalance").asDouble();

                System.out.println("------>"+availableBalance);


                if (paymentDto.getAmount() > availableBalance) {
                    ErrorDto errorDto = ErrorDto.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .status("ERROR")
                            .message("INSUFFICIENT BALANCE FROM THE EXTERNAL SOURCE")
                            .build();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
                }

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

            DisbursementsHistory disbursementsHistory = buildAndSaveDisbursementsHistory(paymentDto, user, DisbursementsStatus.INITIALIZE, new DisbursementsHistory());

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

        String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements";
        HttpHeaders headers = appCommon.getHttpHeaders();

        Map<String , Object> reqMap = buildDisbursementsReq(paymentDto, user);
        disbursementsHistory.setDisbursementsRequest(reqMap.toString());

        disbursementsRepository.save(disbursementsHistory);

        HttpEntity<Map> apiRequestEntity = new HttpEntity<>(buildDisbursementsReq(paymentDto, user), headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);
        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(responseBody, Map.class);

        System.out.println("ResponseEntity ::: = > " + responseEntity);
        System.out.println("ResponseBody ::: = > " + responseEntity.getBody());

        String refId = (String) map.get("transactionReference");
        disbursementsHistory.setDisbursementsResponse(responseBody);
        disbursementsHistory.setDisbursementsTransactionId(refId);

        String status = checkDisbursementsCheckStatus(disbursementsHistory.getDisbursementsTransactionId() , headers);
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
            DisbursementsHistory data = response.get();
            SuccessDto successDto = SuccessDto.builder()
                    .message("DISBURSEMENTS TRANSACTION STATUS IS : .")
                    .code(HttpStatus.OK.value())
                    .status("SUCCESS")
                    .data(data)
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
                                .map(disbursement -> {
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
                                    dto.setStartDate(disbursement.getStartDate());
                                    dto.setEndDate(disbursement.getEndDate());
                                    dto.setDestination(disbursement.getDestination());
                                    dto.setReferenceId(disbursement.getReferenceId());
                                    dto.setNarration(disbursement.getNarration());
                                    dto.setApprovedForTravel(disbursement.isApprovedForTravel());
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


                                    User user = userRepository.findById(disbursement.getUserId()).orElse(null);
                                    if (user != null) {
                                        dto.setUsername(user.getFirstName());
                                        dto.setMobile(user.getPhoneNumber());
                                        dto.setEmail(user.getEmail());
                                    }

                                    return dto;
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


    private void processTravelDisbursement(ApprovalDeclineDto dto , DisbursementsHistory disbursementsHistory) throws JsonProcessingException {
        PaymentDto paymentDto = buildPaymentDtoForTravel(dto , disbursementsHistory);
//        Pageable pageable = PageRequest.of(0, 1); // Limiting to 1 result
        UtilizeUserCredit userCredit = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(disbursementsHistory.getUserId());
        Optional<User> user = userRepository.findByid(disbursementsHistory.getUserId());
        processDisbursements(paymentDto,  user.get(), userCredit , disbursementsHistory);
    }

    private PaymentDto buildPaymentDtoForTravel(ApprovalDeclineDto dto, DisbursementsHistory disbursementsHistory) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAmount(disbursementsHistory.getAmount());
        paymentDto.setReference(disbursementsHistory.getReferenceId());
        paymentDto.setDisbursementsType(DisbursementsType.valueOf(disbursementsHistory.getDisbursementsType()));
        return paymentDto;
    }

    @Override
    public ResponseEntity<?> getApprovedForTravel(ApprovalDeclineDto dto) throws JsonProcessingException {
        try {
            DisbursementsHistory entity = disbursementsRepository.findById(dto.getId()).orElse(null);
//            if (entity != null) {
//                if (dto.isApprove()) {
//                    entity.setApprovedForTravel(true);
//                    processTravelDisbursement(dto, entity);
//                } else {
//                    entity.setApprovedForTravel(false);
//                    entity.setTravelDeclineReason(dto.getReason());
//                }
//                disbursementsRepository.save(entity);
//                String message = "Record with ID " + dto.getId() + " approved for travel.";
//                SuccessDto successDto = SuccessDto.builder()
//                        .message(message)
//                        .code(HttpStatus.OK.value())
//                        .status("APPROVED")
//                        .data(null)
//                        .build();
//                return ResponseEntity.status(HttpStatus.OK).body(successDto);
//            } else {
//                ErrorDto errorDto = ErrorDto.builder()
//                        .message("Record with ID " + dto.getId() + " not found.")
//                        .code(HttpStatus.NOT_FOUND.value())
//                        .status("ERROR")
//                        .build();
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
//            }
        }catch (Exception e){
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
            UtilizeUserCredit userCreditUtilize = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(disbursementsHistory.getUserId());
            calculateUtilization(paymentDto , userCreditUtilize , disbursementsHistory , status);

        }
        disbursementsRepository.save(disbursementsHistory);
    }

    @Scheduled(fixedRate = 150000)
    public void updateDisbursementStatusAndUtilization(){
        System.out.println("Run Schedule:::");
        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository.findByPaymentStatus(DisbursementsStatus.PENDING.name());
        System.out.println("Size Of Pending Status :: " + disbursementsHistoryList.size());
        disbursementsHistoryList.forEach(this::checkDisbursementStatusAndUpdate);
    }

    public String checkDisbursementsCheckStatus(String transactionId , HttpHeaders headers) {
        try {
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

    private DisbursementsHistory buildAndSaveDisbursementsHistory(PaymentDto paymentDto , User user , DisbursementsStatus disbursementsStatus , DisbursementsHistory disbursementsHistory) {
        disbursementsHistory = new DisbursementsHistory();

        try{
            if(paymentDto.getDisbursementsType().name().equals(DisbursementsType.TRAVEL_LOAN.name())){
                disbursementsHistory.setTeamLeadName(paymentDto.getTravelDetails().getTeamLeadName());
                disbursementsHistory.setTeamLeadContactNumber(paymentDto.getTravelDetails().getTeamLeadContactNumber());
                disbursementsHistory.setStartDate(paymentDto.getTravelDetails().getStartDate());
                disbursementsHistory.setEndDate(paymentDto.getTravelDetails().getEndDate());
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

            disbursementsHistory = disbursementsRepository.save(disbursementsHistory);

        }catch (Exception e){
            e.printStackTrace();
        }
        return disbursementsHistory;
    }

    private Map<String , Object> buildDisbursementsReq(PaymentDto paymentDto , User user){
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

        }catch (Exception e){
            e.printStackTrace();
        }
        return paymentMap;
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
