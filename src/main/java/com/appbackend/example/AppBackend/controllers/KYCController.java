package com.appbackend.example.AppBackend.controllers;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.appbackend.example.AppBackend.services.KycCalculationDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.appbackend.example.AppBackend.entities.KycCalculationDetails;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.KYCDataResDto;
//import com.appbackend.example.AppBackend.models.KYCDto;
import com.appbackend.example.AppBackend.models.KYCDocData;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.KYCService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping(value = "/KYC")
public class KYCController {

    Logger logger = LoggerFactory.getLogger(KYCController.class);

    @Autowired
    KYCRepository kycRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private KYCService kycService;
    
    
    @Autowired
    private CreditScoreService creditScoreService;

    @Autowired
    private KycCalculationDetailService kycCalculationDetailService;

//    @Autowired
//    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/docData")
    public ResponseEntity<?> getKYCDocData(@RequestParam Integer id) {




        Object kycDocData = kycService.getUserKYCDocDataById(id);

        if (kycDocData instanceof KYCDocData) {

            return ResponseEntity.ok(kycDocData);

        } else {

            ErrorDto errorDto = ErrorDto.builder().code(404).status("ERROR").message("KYC documents  with id " + id + " are not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        }


    }

    @GetMapping("/data")
    public ResponseEntity<KYCDataResDto> getKYCData(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return new ResponseEntity<>(kycService.getUserKYCDataById(user.getId(), authentication)
                .orElseThrow(() -> new RuntimeException("KYC OF USER NOT FOUND")), HttpStatus.OK);
//
    }



    @PostMapping(value = "/submitData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> kycSubmit(@RequestParam(value = "kycRequest", required = false) String kycRequestString,
                                                   @RequestParam(value = "documentData", required = false) MultipartFile documentData,
                                                   @RequestParam(value = "userImage", required = false) MultipartFile userImage,
                                                   @RequestParam(value = "digitalSignature", required = false) MultipartFile digitalSignature) throws IOException {


        ObjectMapper mapper = new ObjectMapper();
        KYCDataResDto kycResponse = null;
        try {
            KYCDataResDto kycRequest = mapper.readValue(kycRequestString, KYCDataResDto.class);

//            kycResponse = kycService.saveUserKYC(kycRequest, documentData, userImage, digitalSignature);
        }catch (Exception e){
            ErrorDto errorDto = ErrorDto.builder().code(500).status("ERROR").message("Something went wrong.").build();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
        return new ResponseEntity<>(kycResponse, HttpStatus.OK);


    }

    @GetMapping("/calculation/info")
    public Map<String, Object> getAllCreditInfo() {
        logger.info("Inside GetALlCreditInfo Method in Kyc Controller");
        return kycCalculationDetailService.getAllCreditInfoGroupedByWorkplace();
    }
    
    
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateKYC(@PathVariable int id)
    {
        logger.info("Inside UpdateKyc Method in Kyc Controller");
    	return kycService.updateKYC(id);
    }



}
