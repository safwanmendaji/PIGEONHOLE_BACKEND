package com.appbackend.example.AppBackend.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
public class AppCommon {
    private final RestTemplate restTemplate;
    private final String username;
    private final String password;

    public AppCommon(RestTemplate restTemplate, @Value("${payment.username}") String username,
                     @Value("${payment.password}") String password) {
        this.restTemplate = restTemplate;
        this.username = username;
        this.password = password;
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

    public HttpHeaders getHttpHeaders() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tokenResponse = getPigeonToken(headers);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(tokenResponse);
        String accessToken = root.get("access_token").asText();

        headers.setBearerAuth(accessToken);
        return headers;
    }
}
