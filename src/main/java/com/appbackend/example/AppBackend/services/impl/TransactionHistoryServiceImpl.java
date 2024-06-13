package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.entities.TransactionHistory;
import com.appbackend.example.AppBackend.models.TransactionHistoryDto;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.repositories.TransactionHistoryRepository;
import com.appbackend.example.AppBackend.services.TransactionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryServiceImpl implements TransactionHistoryService {


    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private DisbursementsRepository disbursementsRepository;
    @Override
    public List<TransactionHistoryDto> getTransactionHistory(Integer id) {
        List<TransactionHistory> entities = transactionHistoryRepository.findByDisbursementsHistoryId(id);
        List<TransactionHistoryDto> dtoList = entities.stream().map(entity -> {
            TransactionHistoryDto dto = new TransactionHistoryDto();
            dto.setDateTime(entity.getLocalDateTime());
            dto.setTransactionId(entity.getDisbursementsHistory().getDisbursementsTransactionId());
            dto.setStatus(entity.getDisbursementsHistory().getPaymentStatus());
            dto.setAmount(entity.getDisbursementsHistory().getAmount());
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }
}
