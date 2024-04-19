package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appbackend.example.AppBackend.models.PaymentDto;



@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@PostMapping("/disbursement")
	public String payment(@RequestBody PaymentDto paymentDto) {
		return paymentService.payment(paymentDto);

	}

}
