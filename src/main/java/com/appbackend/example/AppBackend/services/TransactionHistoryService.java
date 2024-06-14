package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.TransactionHistoryDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransactionHistoryService {
    ResponseEntity<?> getTransactionHistory(Integer id);
}
