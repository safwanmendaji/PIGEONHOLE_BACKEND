package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.*;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


//@Log
@Service
public class CollectionServiceImpl implements CollectionService {

    Logger log = LoggerFactory.getLogger(CollectionServiceImpl.class);

    @Autowired
    private AppCommon appCommon;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Autowired
    private ReschedulePaymentRecordRepository reschedulePaymentRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private CollectionAmountCalculationRepository collectionAmountCalculationRepository;

    @Autowired
    private MonthlyCollectionInfoRepository monthlyCollectionInfoRepository;

    @Override
    public ResponseEntity<?> getRecollectPayment(CollectionDto collectionDto) {
        log.info("Inside getRecollectPayment method in CollectionServiceImpl");
        try {
            Optional<DisbursementsHistory> disbursementsHistoryOptional = disbursementsRepository.findById(collectionDto.getDisbursementId());
           log.info("disbursementsHistoryOptional ==>> " + disbursementsHistoryOptional);
            if(disbursementsHistoryOptional.isPresent()) {
                DisbursementsHistory disbursementsHistory = disbursementsHistoryOptional.get();
                Map<String , Object> collectionRequestMap = buildCollectionRequest(collectionDto , disbursementsHistory);
                CollectionHistory collectionHistory = buildAndSaveCollectionHistory(collectionDto , disbursementsHistory , collectionRequestMap);

                processPaymentCollection(collectionRequestMap , collectionHistory);
                log.info("====================================");
//                log.info("Transaction Done ++ " + collectionHistory);
                SuccessDto successDto = SuccessDto.builder()
                        .message("Payment request initiated successfully")
                        .code(200)
                        .status("SUCCESS")
                        .build();

                log.info("Response :: " + successDto);
                return ResponseEntity.status(HttpStatus.OK).body(successDto);
            }else {
                ErrorDto errorDto = ErrorDto.builder()
                        .message("Disbursement Not found")
                        .code(404)
                        .status("ERROR")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);        }
    }

    private Map<String, Object> buildCollectionRequest(CollectionDto collectionDto , DisbursementsHistory disbursementsHistory) {
            log.info("Call buildCollectionRequest method.");
            Map<String, Object> paymentMap = new HashMap<>();

            try {

                log.info("account==>>> " + collectionDto.getAccount());
                paymentMap.put("type", "mm");
                paymentMap.put("account", collectionDto.getAccount());
                paymentMap.put("amount", collectionDto.getTotalAmount());
                paymentMap.put("narration", disbursementsHistory.getNarration()+"_COLLECTION");
                paymentMap.put("reference",  UUID.randomUUID());
                log.info("collection request ==>> " + paymentMap);
            }catch (Exception e){
                log.error("Error in create collection request => " + e.getMessage());
                e.printStackTrace();
            }
            return paymentMap;
        }


    private CollectionHistory buildAndSaveCollectionHistory(CollectionDto collectionDto, DisbursementsHistory disbursementsHistory, Map<String, Object> collectionRequestMap) throws JsonProcessingException {
        CollectionHistory collectionHistory = new CollectionHistory();
        collectionHistory.setUser(userRepository.findById(disbursementsHistory.getUserId()).get());
        collectionHistory.setPaymentDate(LocalDateTime.now());
        collectionHistory.setStatus(DisbursementsStatus.INITIALIZE.name());
        collectionHistory.setDisbursementsHistory(disbursementsHistory);
        collectionHistory.setPaymentAmount(collectionDto.getAmount());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(collectionRequestMap); // Convert request body to JSON string
        collectionHistory.setCollectionRequest(requestJson);

       return collectionHistoryRepository.save(collectionHistory);

    }

    private void processPaymentCollection(Map<String, Object> collectionRequestMap, CollectionHistory collectionHistory) throws JsonProcessingException {
            log.info("Call processPaymentCollection method with request ==>> " + collectionRequestMap);
            HttpHeaders headers = appCommon.getHttpHeaders();
            log.info("get header =>> " + headers);
            String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections";
            headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(collectionRequestMap); // Convert request body to JSON string

        HttpEntity<String> apiRequestEntity = new HttpEntity<>(requestJson, headers);


        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);
        String responseBody = responseEntity.getBody();
        Map<?  , ?> map = objectMapper.readValue(responseBody, Map.class);


        System.out.println("ResponseEntity ::: = > " + responseEntity);
        System.out.println("ResponseBody ::: = > " + responseEntity.getBody());


        String refId = (String) map.get("transactionReference");

        log.info("Check The status ::: " + refId);
        String status = checkCollectionCheckStatus(refId , headers);

        collectionHistory.setResponse(responseBody);
        collectionHistory.setResponseTransactionId(refId);
        if(status.equals(DisbursementsStatus.INITIALIZE.name()) && (Integer) map.get("code") >= 200 && (Integer) map.get("code") <= 300)
            status = DisbursementsStatus.PENDING.name();

        collectionHistory.setStatus(status);

        collectionHistoryRepository.save(collectionHistory);


//        return collectionHistory;

    }

    @Override
    public ResponseEntity<?> calculateFinalAmountToPay(CollectionDto collectionDto) {
        log.info("Call calculateFinalAmountToPay method :: " + collectionDto);
        double multiplyAmount = collectionDto.getAmount() * 1.5;
        double mtnCharges = multiplyAmount / 100;

        BigDecimal bd = new BigDecimal(mtnCharges);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        System.out.println("mtnCharges Amount is ==>>> " + bd.doubleValue());

        collectionDto.setMtnCharges(1.5);
        collectionDto.setMtnChargesAmount(bd.doubleValue());
        collectionDto.setTotalAmount(collectionDto.getAmount() + bd.doubleValue());
        System.out.println("mtnCharges Amount is ==>>> " + bd.doubleValue());

        SuccessDto successDto = SuccessDto.builder()
                .message("Success")
                .code(200)
                .status("Success")
                .data(collectionDto)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(successDto) ;
    }

    @Override
    public ResponseEntity<?> getWalletCollections() throws JsonProcessingException {
        String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections/balance";
        HttpHeaders headers = appCommon.getHttpHeaders();

        try {
            URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("network", "MTN")
                    .build()
                    .toUri();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
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

    public void checkCollectionStatusAndUpdate(CollectionHistory collectionHistory){
        String status = checkCollectionCheckStatus(collectionHistory.getResponseTransactionId() , null);
        collectionHistory.setStatus(status);
        if(status.equals(DisbursementsStatus.SUCCEEDED.name())){
            CollectionAmountCalculation collectionAmountCalculationOld = collectionAmountCalculationRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(collectionHistory.getDisbursementsHistory().getId()).get();
            CollectionAmountCalculation collectionAmountCalculation = new CollectionAmountCalculation();
            collectionAmountCalculation.setPayAmount(collectionHistory.getPaymentAmount());
            collectionAmountCalculation.setTotalPayAmount(collectionAmountCalculationOld.getTotalPayAmount() + collectionHistory.getPaymentAmount());
            collectionAmountCalculation.setRemainingPayment(collectionAmountCalculationOld.getRemainingPayment() - collectionHistory.getPaymentAmount());
            collectionAmountCalculation.setUserId(collectionHistory.getUser().getId());
            collectionAmountCalculation.setLastPaymentDate(collectionHistory.getPaymentDate());
            collectionAmountCalculation.setDisbursementsHistory(collectionHistory.getDisbursementsHistory());

             collectionAmountCalculationRepository.save(collectionAmountCalculation);


            MonthlyCollectionInfo monthlyCollectionInfo = monthlyCollectionInfoRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(collectionHistory.getDisbursementsHistory().getId());
            monthlyCollectionInfo.setTotalPayAmountInMonth(monthlyCollectionInfo.getTotalPayAmountInMonth() + collectionHistory.getPaymentAmount());
            if(monthlyCollectionInfo.getMinimumAmount() <= monthlyCollectionInfo.getTotalPayAmountInMonth())
                monthlyCollectionInfo.setPayMinimumAmount(true);

            monthlyCollectionInfoRepository.save(monthlyCollectionInfo);

        }
        collectionHistoryRepository.save(collectionHistory);
    }


    public  String checkCollectionCheckStatus(String transactionId, HttpHeaders headers) {
        try {
            if(headers == null){
                headers = appCommon.getHttpHeaders();
            }
            String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections/" + transactionId;
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

    @Override
    public ResponseEntity<?> reschedulePaymentDate(RescheduleDto rescheduleDto) {
        try{
            Optional<DisbursementsHistory> disbursementsHistoryOptional = disbursementsRepository.findById(rescheduleDto.getDisbursementId());
            if(disbursementsHistoryOptional.isPresent()){
                DisbursementsHistory disbursementsHistory =  disbursementsHistoryOptional.get();
                MonthlyCollectionInfo monthlyCollectionInfo = monthlyCollectionInfoRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId());
                if(monthlyCollectionInfo != null){
                    monthlyCollectionInfo.setIsRescheduled(true);
                    monthlyCollectionInfo.setRescheduleDate(rescheduleDto.getRescheduledDate());
                    monthlyCollectionInfoRepository.save(monthlyCollectionInfo);
                }
                ReschedulePaymentRecord reschedulePaymentRecord = new ReschedulePaymentRecord();
                reschedulePaymentRecord.setRescheduleDate(LocalDate.now());
                reschedulePaymentRecord.setMonthlyCollectionInfo(monthlyCollectionInfo);
                reschedulePaymentRecord.setDisbursementsHistory(disbursementsHistory);
                reschedulePaymentRecord.setUserId(disbursementsHistory.getUserId());

                reschedulePaymentRecordRepository.save(reschedulePaymentRecord);

                SuccessDto successDto = SuccessDto.builder()
                        .message("Reschedule successfully")
                        .code(200)
                        .status("Success")
                        .build();
                return ResponseEntity.status(200).body(successDto);

            }else{
                ErrorDto errorDto = ErrorDto.builder()
                        .message("Disbursement record not found")
                        .code(404)
                        .status("ERROR")
                        .build();
                return ResponseEntity.status(404).body(errorDto);
            }
        }catch (Exception e){
    e.printStackTrace();
            ErrorDto errorDto = ErrorDto.builder()
                    .message("Something when wrong")
                    .code(500)
                    .status("ERROR")
                    .build();
            return ResponseEntity.status(500).body(errorDto);

        }
    }
}
