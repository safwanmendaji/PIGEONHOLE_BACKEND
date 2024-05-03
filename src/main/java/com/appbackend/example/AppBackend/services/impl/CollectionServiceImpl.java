package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.models.PaymentDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Log
@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private AppCommon appCommon;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<?> getRecollectPayment(CollectionDto collectionDto) {
        log.info("Inside getRecollectPayment method in CollectionServiceImpl");
        try {
            HttpHeaders headers = appCommon.getHttpHeaders();
            String apiUrl = "https://api.valueadditionmicrofinance.com/v1/collections";
            HttpEntity<CollectionDto> apiRequestEntity = new HttpEntity<>(collectionDto, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);
            String responseBody = responseEntity.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });

            SuccessDto successDto = SuccessDto.builder()
                    .message("Recollect payment successful")
                    .code(HttpStatus.OK.value())
                    .status("SUCCESS")
                    .data(resultMap)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred");
        }
    }
}
