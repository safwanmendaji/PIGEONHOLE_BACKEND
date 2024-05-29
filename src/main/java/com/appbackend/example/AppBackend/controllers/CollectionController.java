package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@Log
@RestController
@RequestMapping("/collection")
public class CollectionController {



    @Autowired
    private CollectionService collectionService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<?> getRecollectPayment(@RequestBody CollectionDto collectionDto) throws JsonProcessingException {
        log.info("Inside Collection of Payment getRecollectPayment method Controller");
        return  collectionService.getRecollectPayment(collectionDto);
    }

    @GetMapping("/wallet/collection")
    public ResponseEntity<?> getWalletCollections() throws JsonProcessingException {
        return collectionService.getWalletCollections();
    }

    @GetMapping("/final/amountToPay")
    public ResponseEntity<?> calculateFinalAmountToPay(@RequestBody CollectionDto collectionDto)  {
        return collectionService.calculateFinalAmountToPay(collectionDto);
    }
}
