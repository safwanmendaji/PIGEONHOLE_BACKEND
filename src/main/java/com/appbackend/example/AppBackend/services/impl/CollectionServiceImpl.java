package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.PaymentDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;



@Service
public class CollectionServiceImpl implements CollectionService {

    Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);


    @Autowired
    private AppCommon appCommon;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DisbursementsRepository disbursementsRepository;

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

        try {
            logger.info("Inside getRecollectPayment Method in CollectionServiceImpl");
            Optional<DisbursementsHistory> disbursementsHistoryOptional = disbursementsRepository.findById(collectionDto.getDisbursementId());
            if(disbursementsHistoryOptional.isPresent()) {
                DisbursementsHistory disbursementsHistory = disbursementsHistoryOptional.get();
                Map<String , Object> collectionRequestMap = buildCollectionRequest(collectionDto , disbursementsHistory);
                CollectionHistory collectionHistory = buildAndSaveCollectionHistory(collectionDto , disbursementsHistory , collectionRequestMap);

                collectionHistory = processPaymentCollection(collectionRequestMap , collectionHistory);

                SuccessDto successDto = SuccessDto.builder()
                        .message("Recollect payment successful")
                        .code(HttpStatus.OK.value())
                        .status("SUCCESS")
                        .data(collectionHistory)
                        .build();

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
            logger.error(String.valueOf(e));
            e.printStackTrace();
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);        }
    }

    private Map<String, Object> buildCollectionRequest(CollectionDto collectionDto , DisbursementsHistory disbursementsHistory) {

            Map<String, Object> paymentMap = new HashMap<>();

            try {
                logger.info("Inside BuildCollectionRequest Method in CollectionServiceImpl");

                System.out.println("account==>>> " + collectionDto.getAccount());
                paymentMap.put("type", "mm");
                paymentMap.put("account", collectionDto.getAccount());
                paymentMap.put("amount", collectionDto.getAmount());
                paymentMap.put("narration", disbursementsHistory.getNarration()+"_COLLECTION");
                paymentMap.put("reference",  UUID.randomUUID());
            }catch (Exception e){
                logger.error(String.valueOf(e));
                e.printStackTrace();
            }
            return paymentMap;
        }


    private CollectionHistory buildAndSaveCollectionHistory(CollectionDto collectionDto, DisbursementsHistory disbursementsHistory, Map<String, Object> collectionRequestMap) {
        logger.info("Inside buildAndSaveCollectionHistory Method in CollectionServiceImpl");
        CollectionHistory collectionHistory = new CollectionHistory();
        collectionHistory.setUser(userRepository.findByid(disbursementsHistory.getUserId()).get());
        collectionHistory.setPaymentDate(LocalDateTime.now());
        collectionHistory.setStatus(DisbursementsStatus.INITIALIZE.name());
        collectionHistory.setDisbursementsHistory(disbursementsHistory);
        collectionHistory.setPaymentAmount(collectionDto.getAmount());
        collectionHistory.setCollectionRequest(collectionRequestMap.toString());

        collectionHistory  = collectionHistoryRepository.save(collectionHistory);
        return collectionHistory;
    }

    private CollectionHistory processPaymentCollection(Map<String, Object> collectionRequestMap, CollectionHistory collectionHistory) throws JsonProcessingException {

        logger.info("Inside processPaymentCollection Method in CollectionServiceImpl");
            HttpHeaders headers = appCommon.getHttpHeaders();
            String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections";
            HttpEntity<Map> apiRequestEntity = new HttpEntity<>(collectionRequestMap, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);
            String responseBody = responseEntity.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });

        System.out.println("ResponseEntity ::: = > " + responseEntity);
        System.out.println("ResponseBody ::: = > " + responseEntity.getBody());

        String refId = (String) resultMap.get("transactionReference");
        collectionHistory.setResponse(responseBody);
        collectionHistory.setResponseTransactionId(refId);

        collectionHistory = collectionHistoryRepository.save(collectionHistory);


        return collectionHistory;

    }

    @Override
    public ResponseEntity<?> calculateFinalAmountToPay(CollectionDto collectionDto) {

        logger.info("Inside calculateFinalAmountToPay Method in CollectionServiceImpl");
        double multiplyAmount = collectionDto.getAmount() * 1.5;
        double mtnCharges = multiplyAmount / 100;
        System.out.println("mtnCharges Amount is ==>>> " + mtnCharges);
        collectionDto.setMtnCharges(1.5);
        collectionDto.setMtnChargesAmount(mtnCharges);
        collectionDto.setTotalAmount(collectionDto.getAmount() + mtnCharges);
        System.out.println("mtnCharges Amount is ==>>> " + mtnCharges);

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
            logger.info("Inside getWalletCollections Method in CollectionServiceImpl");
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
            logger.error(String.valueOf(ex));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve wallet collections:" + ex.getMessage());
        }
    }

    public void checkCollectionStatusAndUpdate(CollectionHistory collectionHistory){
        logger.info("Inside checkCollectionStatusAndUpdate Method in CollectionServiceImpl");
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

             collectionAmountCalculationRepository.save(collectionAmountCalculation);


            MonthlyCollectionInfo monthlyCollectionInfo = monthlyCollectionInfoRepository.findByDisbursementsHistoryId(collectionHistory.getDisbursementsHistory().getId());
            monthlyCollectionInfo.setTotalPayAmountInMonth(monthlyCollectionInfo.getTotalPayAmountInMonth() + collectionHistory.getPaymentAmount());
            if(monthlyCollectionInfo.getMinimumAmount() <= monthlyCollectionInfo.getTotalPayAmountInMonth())
                monthlyCollectionInfo.setPayMinimumAmount(true);

            monthlyCollectionInfoRepository.save(monthlyCollectionInfo);

        }
        collectionHistoryRepository.save(collectionHistory);
    }



    public  String checkCollectionCheckStatus(String transactionId, HttpHeaders headers) {
        try {
            logger.info("Inside checkCollectionCheckStatus Method in CollectionServiceImpl");
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
            logger.error(String.valueOf(e));
            throw new RuntimeException(e);
        }
    }


}
