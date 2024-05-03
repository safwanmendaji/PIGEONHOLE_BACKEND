package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.models.PaymentDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

public interface CollectionService {
    ResponseEntity<?> getRecollectPayment(CollectionDto collectionDto) throws JsonProcessingException;
}
