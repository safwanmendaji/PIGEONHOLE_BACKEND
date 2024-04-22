package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appbackend.example.AppBackend.models.PaymentDto;

import java.util.Map;


@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@PostMapping("/disbursement")
	public ResponseEntity<?> payment(@RequestBody PaymentDto paymentDto) {
		return paymentService.payment(paymentDto);

	}

}
