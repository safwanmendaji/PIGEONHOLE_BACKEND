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


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import java.security.Key;

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


    private String getPigeonToken(HttpHeaders headers) {
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        String requestBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String tokenUrl = "https://api.valueadditionmicrofinance.com/v1/token";

        try {
            ResponseEntity<String> tokenResponseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

            String tokenResponse = tokenResponseEntity.getBody();
            return tokenResponse;
        } catch (Exception e) {
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
//        headers.setContentType(MediaType.valueOf("application/json"));
        return headers;
    }

    private static final String SECRET_KEY = "pigeonhole123567"; // 16 bytes for AES-128

    public static String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Specify mode and padding
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Specify mode and padding
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static Key generateKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe


    public static String generateUniqueCode() {

        int randomInt = secureRandom.nextInt(900000) + 100000; // Generates a number between 100000 and 999999
        return String.valueOf(randomInt);
    }


    public String verifyReferenceCode(String encryptedReferCode, String email, String mobile) {
        try {
            String decryptedReferCode = decrypt(encryptedReferCode);
            String[] parts = decryptedReferCode.split("\\|");
            if (parts.length == 2) {
                String decryptedEmail = parts[0];
                String decryptedMobile = parts[1];
                if (decryptedEmail.equals(email) && decryptedMobile.equals(mobile)) {
                    return "Reference code verified successfully.";
                } else {
                    return "Reference code is invalid.";
                }
            } else {
                return "Invalid format of reference code.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error verifying reference code.";
        }
    }

}

