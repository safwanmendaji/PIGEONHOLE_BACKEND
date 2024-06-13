package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.TransactionHistoryDto;

import java.util.List;

public interface TransactionHistoryService {
    List<TransactionHistoryDto> getTransactionHistory(Integer id);
}
