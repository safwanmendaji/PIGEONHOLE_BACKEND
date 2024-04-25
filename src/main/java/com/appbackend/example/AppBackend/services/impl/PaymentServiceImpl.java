package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.DisbursementsType;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appbackend.example.AppBackend.models.PaymentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${payment.username}")
    private String username;


    @Value("${payment.password}")
    private String password;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Override
    public ResponseEntity<?> payment(PaymentDto paymentDto) {
       try {
           Optional<User> optionalUser = userRepository.findByid(paymentDto.getUserId());

           if(!optionalUser.isPresent()){
               ErrorDto errorDto = ErrorDto.builder()
                       .code(HttpStatus.NOT_FOUND.value())
                       .status("ERROR")
                       .message("USER NOT FOUND FOR THIS USERID : " + paymentDto.getUserId()).build();
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
           }

           User user = optionalUser.get();

           UUID uuid = UUID.randomUUID();

           paymentDto.setReference(uuid);
           if(paymentDto.getDisbursementsType().name().equals(DisbursementsType.TRAVEL_LOAN.name())){
               buildAndSaveDisbursementsHistory(paymentDto, user, DisbursementsStatus.REQUESTED , new DisbursementsHistory());
               SuccessDto successDto = SuccessDto.builder()
                       .message("YOUR DISBURSEMENT REQUEST SUBMITTED SUCCESSFULLY WHEN ADMIN APPROVE WE START PROCESS YOUR DISBURSEMENT." ).code(HttpStatus.OK.value()).status("SUCCESS").build();
               return ResponseEntity.status(HttpStatus.OK).body(successDto);
           }else {
               String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements";
               DisbursementsHistory disbursementsHistory = buildAndSaveDisbursementsHistory(paymentDto, user, DisbursementsStatus.INITIALIZE , new DisbursementsHistory());
               HttpHeaders headers = getHttpHeaders();

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

               SuccessDto successDto = SuccessDto.builder().message("DISBURSEMENTS TRANSACTION STATUS IS : ." + disbursementsHistory.getPaymentStatus()).code(HttpStatus.OK.value()).status("SUCCESS").build();

               return ResponseEntity.status(HttpStatus.OK).body(successDto);
           }
        } catch (Exception ex) {
           ErrorDto errorDto = ErrorDto.builder()
                   .code(HttpStatus.NOT_FOUND.value())
                   .status("ERROR")
                   .message(ex.getMessage())
                   .build();

           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);

       }
    }

    private HttpHeaders getHttpHeaders() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tokenResponse = getPigeonToken(headers);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(tokenResponse);
        String accessToken = root.get("access_token").asText();

        headers.setBearerAuth(accessToken);
        return headers;
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

        Map<String, Long> groupedData = allDisbursements.stream()
                .collect(Collectors.groupingBy(DisbursementsHistory::getDisbursementsType, Collectors.counting()));

        List<Map<String, String>> formattedData = groupedData.entrySet().stream()
                .map(entry -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("disbursementType", entry.getKey());
                    map.put("count", String.valueOf(entry.getValue()));
                    return map;
                })
                .collect(Collectors.toList());

        SuccessDto successDto = SuccessDto.builder()
                .message("Disbursement transaction status")
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .data(formattedData)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(successDto);
    }

    @Override
    public ResponseEntity<?> getApprovedForTravel(int id) {
        DisbursementsHistory entity = disbursementsRepository.findById(id).orElse(null);
        if (entity != null) {
            entity.setApprovedForTravel(true);
            disbursementsRepository.save(entity);
            String message = "Record with ID " + id + " approved for travel.";
            SuccessDto successDto = SuccessDto.builder()
                    .message(message)
                    .code(HttpStatus.OK.value())
                    .status("APPROVED")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record with ID " + id + " not found.");
        }
    }



    public String checkDisbursementsCheckStatus(String transactionId , HttpHeaders headers) {
        try {
            if(headers == null){
                headers = getHttpHeaders();
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
            disbursementsHistory.setPaymentStatus(disbursementsStatus.INITIALIZE.name());
            disbursementsHistory.setDisbursementsType(paymentDto.getDisbursementsType().name());
            disbursementsHistory.setNarration(paymentDto.getDisbursementsType().name());
            disbursementsHistory.setReferenceId(paymentDto.getReference());

            disbursementsHistory = disbursementsRepository.save(disbursementsHistory);

        }catch (Exception e){
            e.printStackTrace();
        }
        return disbursementsHistory;
    }


    private String getPigeonToken(HttpHeaders headers){
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        String requestBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String tokenUrl = "https://api.valueadditionmicrofinance.com/v1/token";

        try {
            ResponseEntity<String> tokenResponseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

            String tokenResponse = tokenResponseEntity.getBody();
            return tokenResponse;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Map<String , Object> buildDisbursementsReq(PaymentDto paymentDto , User user){
        Map<String, Object> paymentMap = new HashMap<>();

        try {
            paymentMap.put("type", "mm");
            paymentMap.put("account", user.getPhoneNumber());
            paymentMap.put("amount", paymentDto.getAmount());
            paymentMap.put("narration", paymentDto.getDisbursementsType().name());
            paymentMap.put("reference", paymentDto.getReference());

        }catch (Exception e){
            e.printStackTrace();
        }
        return paymentMap;
    }
}
