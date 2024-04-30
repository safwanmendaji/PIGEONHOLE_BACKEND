package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.ApprovalDeclineDto;
import com.appbackend.example.AppBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.appbackend.example.AppBackend.models.PaymentDto;


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


	@GetMapping("/disbursement/history")
	public ResponseEntity<?> getAllDisbursementHistoryGroupedByType() {
		return    paymentService.getAllDisbursementHistoryGroupedByType();

	}

	@PutMapping("/disbursement/travel/approve/")
	public ResponseEntity<?> getApprovedForTravel(@RequestBody ApprovalDeclineDto dto){
		return paymentService.getApprovedForTravel(dto);
	}

	@GetMapping("/disbursement/history/user")
	public ResponseEntity<?>  getDisbursementHistoryOfUser(Authentication authentication){
		return paymentService.getDisbursementHistoryOfUser(authentication);
	}

}
