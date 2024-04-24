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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appbackend.example.AppBackend.models.PaymentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

           if(paymentDto.getDisbursementsType().name().equals(DisbursementsType.TRAVEL_LOAN)){
               buildAndSaveDisbursementsHistory(paymentDto, user, DisbursementsStatus.REQUESTED , new DisbursementsHistory());
               SuccessDto successDto = SuccessDto.builder().message("YOUR DISBURSEMENT REQUEST SUBMITTED SUCCESSFULLY WHEN ADMIN APPROVE WE START PROCESS YOUR DISBURSEMENT." ).code(HttpStatus.OK.value()).status("SUCCESS").build();
               return ResponseEntity.status(HttpStatus.OK).body(successDto);
           }else {

               String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements";
               DisbursementsHistory disbursementsHistory = buildAndSaveDisbursementsHistory(paymentDto, user, DisbursementsStatus.INITIALIZE , new DisbursementsHistory());
               HttpHeaders headers = new HttpHeaders();
               headers.setContentType(MediaType.APPLICATION_JSON);
               String tokenResponse = getPigeonToken(headers);
               ObjectMapper mapper = new ObjectMapper();
               JsonNode root = mapper.readTree(tokenResponse);
               String accessToken = root.get("access_token").asText();

               headers.setBearerAuth(accessToken);

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

                   String status = checkDisbursementsStatus(disbursementsHistory.getDisbursementsTransactionId() , headers);

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

    private String checkDisbursementsStatus(String transactionId , HttpHeaders headers) {
        return transactionId;
    }

    private DisbursementsHistory buildAndSaveDisbursementsHistory(PaymentDto paymentDto , User user , DisbursementsStatus disbursementsStatus , DisbursementsHistory disbursementsHistory) {
        disbursementsHistory = new DisbursementsHistory();

        try{
            disbursementsHistory.setAmount(paymentDto.getAmount());
            disbursementsHistory.setUserId(user.getId());
            disbursementsHistory.setCreatedOn(LocalDateTime.now());
            disbursementsHistory.setPaymentStatus(disbursementsStatus.INITIALIZE);
            disbursementsHistory.setDisbursementsType(paymentDto.getDisbursementsType());

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
            long currentTimestamp = Instant.now().toEpochMilli();
            String ref = "SNM"+currentTimestamp;

            paymentMap.put("type", "mm");
            paymentMap.put("account", user.getPhoneNumber());
            paymentMap.put("amount", paymentDto.getAmount());
            paymentMap.put("narration", paymentDto.getDisbursementType());
            paymentMap.put("reference", ref);

        }catch (Exception e){
            e.printStackTrace();
        }
        return paymentMap;
    }
}
