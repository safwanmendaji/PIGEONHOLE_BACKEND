package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appbackend.example.AppBackend.models.PaymentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${payment.username}")
    private String username;
    
    
    @Value("${payment.password}")
    private String password;

    public String payment(PaymentDto paymentDto) {
       
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        String requestBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String tokenUrl = "https://api.valueadditionmicrofinance.com/v1/token";
        String apiUrl = "https://api.valueadditionmicrofinance.com/v1/disbursements";

        try {
            ResponseEntity<String> tokenResponseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

            String tokenResponse = tokenResponseEntity.getBody();

          
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(tokenResponse);
            String accessToken = root.get("access_token").asText();

          
            HttpEntity<PaymentDto> apiRequestEntity = new HttpEntity<>(paymentDto, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                return responseBody;
            } else {
                return "Error: " + responseEntity.getStatusCodeValue() + " " + responseEntity.getStatusCode();
            }
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }
}
