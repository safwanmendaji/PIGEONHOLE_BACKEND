package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.entities.CollectionHistory;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.models.RescheduleDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CollectionService {
    ResponseEntity<?> getRecollectPayment(CollectionDto collectionDto) throws JsonProcessingException;
    ResponseEntity<?> calculateFinalAmountToPay(CollectionDto collectionDto);
    ResponseEntity<?> getWalletCollections() throws JsonProcessingException;
    void checkCollectionStatusAndUpdate(CollectionHistory collectionHistory);

    ResponseEntity<?> reschedulePaymentDate(RescheduleDto rescheduleDto, List<String> roles);
}
