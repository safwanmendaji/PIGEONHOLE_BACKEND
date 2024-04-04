package com.appbackend.example.AppBackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.CreditScoreDTO;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;

@RestController
@RequestMapping("/creditscore")
public class CreditScoreController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private CreditScoreService creditScoreService; 

	@GetMapping("/getscore")
	public ResponseEntity<?> getCreditScore() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    if (authentication != null && authentication.getPrincipal() instanceof User) {
	        User user = (User) authentication.getPrincipal();
	        
	        CreditScoreDTO creditScore = creditScoreService.findByUserId(user.getId());
	        
	        if (creditScore != null) {
	            return ResponseEntity.ok(creditScore);
	        }
	    }
	    
	    // If the user is not authenticated or credit score is not found, return 404
	    return ResponseEntity.notFound().build();
	}

}
