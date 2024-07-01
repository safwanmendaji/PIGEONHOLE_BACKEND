package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.models.PaymentDto;
import com.appbackend.example.AppBackend.models.ReferDto;
import com.appbackend.example.AppBackend.models.RescheduleDto;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collection")
public class CollectionController {

    Logger log = LoggerFactory.getLogger(CollectionController.class);

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<?> getRecollectPayment(@RequestBody CollectionDto collectionDto) throws JsonProcessingException {
        log.info("Inside Collection of Payment getRecollectPayment method Controller");
        return collectionService.getRecollectPayment(collectionDto);
    }

    @GetMapping("/wallet/collection")
    public ResponseEntity<?> getWalletCollections() throws JsonProcessingException {
        return collectionService.getWalletCollections();
    }

    @PostMapping("/final/amountToPay")
    public ResponseEntity<?> calculateFinalAmountToPay(@RequestBody CollectionDto collectionDto) {
        log.info("Inside calculateFinalAmountToPay method Controller");
        return collectionService.calculateFinalAmountToPay(collectionDto);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<?> reschedulePayment(@RequestBody RescheduleDto rescheduleDto){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        return collectionService.reschedulePaymentDate(rescheduleDto , roles);
    }


}
