package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.entities.CollectionHistory;
import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.entities.TransactionHistory;
import com.appbackend.example.AppBackend.enums.PaymentFor;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.models.TransactionHistoryDto;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.repositories.TransactionHistoryRepository;
import com.appbackend.example.AppBackend.services.TransactionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryServiceImpl implements TransactionHistoryService {


    Logger log = LoggerFactory.getLogger(TransactionHistoryServiceImpl.class);

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private DisbursementsRepository disbursementsRepository;
    @Override
    public ResponseEntity<?> getTransactionHistory(Integer userId) {
        try {
            log.info("getTransactionHistory Method Call :::");
            List<TransactionHistory> entities = transactionHistoryRepository.findByUserIdOrderByTransactionHistoryIdDesc(userId);
            log.info("Total size of TransactionHistory Found ::: " + entities.size());

            List<TransactionHistoryDto> dtoList = entities.stream()
                    .map(this::toTransactionHistoryDto)
                    .collect(Collectors.toList());

            SuccessDto successDto = SuccessDto.builder()
                    .message("Transaction History Record Find")
                    .code(HttpStatus.OK.value())
                    .status("SUCCESS")
                    .data(dtoList)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);

        }catch (Exception e){
            ErrorDto errorDto = ErrorDto.builder()
                    .message("Some thing when wrong. " + e.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("ERROR")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }

    }

    private TransactionHistoryDto toTransactionHistoryDto(TransactionHistory entity) {
        TransactionHistoryDto dto = new TransactionHistoryDto();
        dto.setDateTime(entity.getLocalDateTime());
        dto.setTransactionHistoryId(entity.getTransactionHistoryId());

        if (entity.getDisbursementsHistory() != null) {
            setDisbursementsHistoryDetails(dto, entity.getDisbursementsHistory());
        } else if (entity.getCollectionHistory() != null) {
            setCollectionHistoryDetails(dto, entity.getCollectionHistory());
        }

        return dto;
    }

    private void setDisbursementsHistoryDetails(TransactionHistoryDto dto, DisbursementsHistory disbursementsHistory) {
        dto.setTransactionId(disbursementsHistory.getDisbursementsTransactionId());
        dto.setStatus(disbursementsHistory.getPaymentStatus());
        dto.setAmount(disbursementsHistory.getAmount());
        dto.setPaymentFor(PaymentFor.DISBURSEMENT.name());
    }

    private void setCollectionHistoryDetails(TransactionHistoryDto dto, CollectionHistory collectionHistory) {
        dto.setTransactionId(collectionHistory.getRequestTransactionId());
        dto.setStatus(collectionHistory.getStatus());
        dto.setAmount(collectionHistory.getPaymentAmount());
        dto.setPaymentFor(PaymentFor.REPAYMENT.name());
    }
}
