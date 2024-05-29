package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.ApprovalDeclineDto;
import com.appbackend.example.AppBackend.models.CollectionDto;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.appbackend.example.AppBackend.services.impl.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.io.IOException;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private StorageService storageService;

	@PostMapping("/disbursement")
	public ResponseEntity<?> payment(@RequestBody PaymentDto paymentDto) {
		logger.info("Inside payment Disbursement  Method in Payment Controller");
		return paymentService.payment(paymentDto);

	}

	@GetMapping("/disbursement/status/{id}")
	public ResponseEntity<?> checkDisbursementStatus(@PathVariable String id){
		logger.info("Inside check Disbursement Status Method in Payment Controller");
		return paymentService.checkDisbursementsStatus(id , null);
	}

	@GetMapping("/disbursement/{id}")
	public ResponseEntity<?> getDisbursementHistoryById(@PathVariable int id){
		logger.info("Inside DisbursementHistoryById  Method in Payment Controller");
		return paymentService.getByDisbursementHistoryById(id);
	}


	@GetMapping("/disbursement/history")
	public ResponseEntity<?> getAllDisbursementHistoryGroupedByType() {
		logger.info("Inside Disbursement History By Type Method  in Payment Controller");
		return    paymentService.getAllDisbursementHistoryGroupedByType();

	}

	@PutMapping("/disbursement/travel/approve")
	public ResponseEntity<?> getApprovedForTravel(@RequestBody ApprovalDeclineDto dto) throws JsonProcessingException {
		logger.info("Inside Get ApprovedFor Travel Method  in Payment Controller");
		return paymentService.getApprovedForTravel(dto);
	}

	@GetMapping("/disbursement/history/user")
	public ResponseEntity<?>  getDisbursementHistoryOfUser(Authentication authentication){
		logger.info("Inside GetDisbursementHistoryOfUser  Method in Payment Controller");
		return paymentService.getDisbursementHistoryOfUser(authentication);
	}

//	@GetMapping("/disbursement/status/{id}")
//	public ResponseEntity<?>  getDisbursementStatus(@PathVariable String id){
//		return paymentService.getDisbursementStatus(id);
//	}




	@PostMapping("/disbursement/document/{disbursementId}")
	public ResponseEntity<?> uploadDisbursementDocument(@RequestParam("file") MultipartFile file,@PathVariable int disbursementId) {
		logger.info("Inside DisbursementDocumentUpload Method in Payment Controller  ");
		return paymentService.uploadDisbursementDocument(file,disbursementId);
	}

	@GetMapping("/wallet/balance")
	public ResponseEntity<?>  getWalletBalance() throws JsonProcessingException {
		logger.info("Inside Get Wallet Balance Method in Payment Controller ");
		return paymentService.getWalletBalance();
	}



	@GetMapping("/check/success")
	public ResponseEntity<?> test()  {
		return paymentService.test();
	}




}
