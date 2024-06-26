package com.appbackend.example.AppBackend.controllers;



import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.CreditScoreDTO;
import com.appbackend.example.AppBackend.models.CreditScoreDtoDemo;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;

@RestController
@Log
@RequestMapping("/creditscore")
public class CreditScoreController {

	Logger logger = LoggerFactory.getLogger(CreditScoreController.class);
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private CreditScoreService creditScoreService; 

	@GetMapping("/getscore")
	public ResponseEntity<?> getCreditScore() {
		try {
			logger.info("Inside GetCreditScore Method in CreditScore Controller");
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
		}catch (Exception e){
			logger.error(String.valueOf(e));
			e.printStackTrace();
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("SOME THING WHEN WRONG.").build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
		}
	}

	@PostMapping("/calculate/{email}")
	public ResponseEntity<?> getCreditScoreDemo(@RequestBody CreditScoreDtoDemo creditScoreDtoDemo, @PathVariable String email) {
		User user=  null;
		try {
			logger.info("Inside GetCreditScoreDemo Method in CreditScore Controller");
			user = userService.getUserByEmail(email).get();
		}catch (Exception e){
			logger.error(String.valueOf(e));
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("USER WITH EMAIL " + email + " IS NOT FOUND ").build();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
		}
		return ResponseEntity.ok().build();
	}
	
	


	

}
