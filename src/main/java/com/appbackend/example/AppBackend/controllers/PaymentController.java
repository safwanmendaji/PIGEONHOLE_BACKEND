package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.appbackend.example.AppBackend.models.PaymentDto;

import java.util.List;
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

	@GetMapping("/disbursement/status/{id}")
	public ResponseEntity<?> checkDisbursementStatus(@PathVariable String id){
		return paymentService.checkDisbursementsStatus(id , null);
	}

	@GetMapping("/disbursement/{id}")
	public ResponseEntity<?> getDisbursementHistoryById(@PathVariable int id){

		return paymentService.getByDisbursementHistoryById(id);
	}


	@GetMapping("/disbursement/all")
	public ResponseEntity<?> getAllDisbursementHistoryGroupedByType() {
		ResponseEntity<?>  groupedData = paymentService.getAllDisbursementHistoryGroupedByType();
		return ResponseEntity.ok(groupedData);
	}

	@PutMapping("/disbursement/aprovedfortravel/{id}")
	public ResponseEntity<?> getApprovedForTravel(int id){
		return paymentService.getApprovedForTravel(id);
	}

}
