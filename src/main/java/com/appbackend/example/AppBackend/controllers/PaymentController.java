package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.ApprovalDeclineDto;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.appbackend.example.AppBackend.services.impl.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.io.IOException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.appbackend.example.AppBackend.models.PaymentDto;
import org.springframework.web.multipart.MultipartFile;

@Log
@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private StorageService storageService;

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

	@PutMapping("/disbursement/travel/approve")
	public ResponseEntity<?> getApprovedForTravel(@RequestBody ApprovalDeclineDto dto) throws JsonProcessingException {
		return paymentService.getApprovedForTravel(dto);
	}

	@GetMapping("/disbursement/history/user")
	public ResponseEntity<?>  getDisbursementHistoryOfUser(Authentication authentication){
		return paymentService.getDisbursementHistoryOfUser(authentication);
	}

//	@GetMapping("/disbursement/status/{id}")
//	public ResponseEntity<?>  getDisbursementStatus(@PathVariable String id){
//		return paymentService.getDisbursementStatus(id);
//	}

	@PostMapping("/collections")
	public ResponseEntity<?>   getRecollectPayment(@RequestBody CollectionDto collectionDto) throws JsonProcessingException {
		log.info("Inside Collection of Payment getRecollectPayment method Controller");
		return  collectionService.getRecollectPayment(collectionDto);
	}


	@PostMapping("/disbursement/document/id")
	public ResponseEntity<?> uploadDisbursementDocument(@RequestParam("file") MultipartFile file,@PathVariable int disbursementId) {
		return paymentService.uploadDisbursementDocument(file,disbursementId);
	}



}
