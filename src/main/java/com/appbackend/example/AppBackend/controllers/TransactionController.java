package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.TransactionHistoryDto;
import com.appbackend.example.AppBackend.services.TransactionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionHistoryService transactionHistoryService;


    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Integer userId){
        return transactionHistoryService.getTransactionHistory(userId);
    }

}
