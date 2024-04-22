package com.appbackend.example.AppBackend.services;


import com.appbackend.example.AppBackend.models.PaymentDto;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PaymentService {
	ResponseEntity<?> payment(PaymentDto paymentDto);
}
