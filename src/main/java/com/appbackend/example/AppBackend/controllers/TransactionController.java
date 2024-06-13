package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.TransactionHistoryDto;
import com.appbackend.example.AppBackend.services.TransactionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionHistoryService transactionHistoryService;


    @GetMapping("/history")
    public List<TransactionHistoryDto> getTransactionHistory(@RequestParam Integer id){
        return transactionHistoryService.getTransactionHistory(id);
    }

}
