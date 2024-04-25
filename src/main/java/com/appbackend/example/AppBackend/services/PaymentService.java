package com.appbackend.example.AppBackend.services;


import com.appbackend.example.AppBackend.models.PaymentDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
	ResponseEntity<?> payment(PaymentDto paymentDto);

	ResponseEntity<?> checkDisbursementsStatus(String transactionId , HttpHeaders headers);

	ResponseEntity<?> getByDisbursementHistoryById(int id);


	ResponseEntity<?> getAllDisbursementHistoryGroupedByType();

	ResponseEntity<?> getApprovedForTravel(int id);
}
