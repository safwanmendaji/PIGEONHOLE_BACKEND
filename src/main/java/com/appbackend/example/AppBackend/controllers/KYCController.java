package com.appbackend.example.AppBackend.controllers;


import com.appbackend.example.AppBackend.config.AuthController;
import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.KYCDataResDto;
//import com.appbackend.example.AppBackend.models.KYCDto;

import com.appbackend.example.AppBackend.models.KYCDocData;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.KYCService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.ContentType;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;


@Controller
@Slf4j
@RequestMapping(value = "/KYC")
public class KYCController {

    @Autowired
    KYCRepository kycRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private KYCService kycService;

//    @Autowired
//    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/docData")
    public ResponseEntity<?> getKYCDocData(@RequestParam Integer id) {
//        User user = (User) authentication.getPrincipal();

        log.info("DOC DATA HERE HII");
//        User user = userRepository.findByid(id).orElseThrow(()->{""});
//        User user1=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//        System.out.println("ID IS HERE "+user1.getId()+"/n/n");
//        log.info(user.toString());

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

//        kycService.getUserKYCById(user.getId());


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
            kycResponse = kycService.saveUserKYC(kycRequest, documentData, userImage, digitalSignature);
        }catch (Exception e){
            ErrorDto errorDto = ErrorDto.builder().code(500).status("ERROR").message("Something went wrong.").build();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
        return new ResponseEntity<>(kycResponse, HttpStatus.OK);


    }

}
