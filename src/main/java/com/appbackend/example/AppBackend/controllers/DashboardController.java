package com.appbackend.example.AppBackend.controllers;

import java.util.List;
import java.util.Map;

import com.appbackend.example.AppBackend.models.ApprovalDeclineDto;
import com.appbackend.example.AppBackend.models.UserDtoForUpdate;
import com.appbackend.example.AppBackend.security.JwtHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.UserKYCDto;
import com.appbackend.example.AppBackend.models.PaginationModel.Filter;
import com.appbackend.example.AppBackend.models.PaginationModel.SortField;
import com.appbackend.example.AppBackend.services.DashBoardService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;
import com.appbackend.example.AppBackend.services.AdminServices.UserDataService;

@Log
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/dashboard")
public class DashboardController {

	@Autowired
	UserDataService userDataService;

	@Autowired
	CreditScoreService creditScoreService;



	@Autowired
	private DashBoardService dashboardService;

	@Autowired
	private JwtHelper jwtHelper;

	@PostMapping("/kyc/users")
	public ResponseEntity<?> getUsersData(@RequestBody Map<String, Object> searchInfo) {

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		System.out.println("Principle name is \n\n");
		System.out.println(user.getFirstName());

		List<SortField> sortFields = (List<SortField>) searchInfo.get("sortFields");
		List<Filter> filters = (List<Filter>) searchInfo.get("filters");

		System.out.println(sortFields);
		System.out.println(filters);

		return null;

	}

	@GetMapping("/users")
	public ResponseEntity<?> getAllUsers() {
		return dashboardService.getAllUsers();
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<?> getAllUserById(@PathVariable int id) {
		return dashboardService.getUserAndKYCByUserId(id);
	}

	@PostMapping("/calculate_creditscore")
	public ResponseEntity<?> calculateCreditScore(@RequestBody Map<String, Map<String, Object>> objectMap) {
		creditScoreService.calculateCreditScore(objectMap);
		return null;

	}

	@PutMapping("/kyc/update")
	public ResponseEntity<?> updateKyc(@RequestBody UserKYCDto userKycDto) {
		return dashboardService.updateUserKyc(userKycDto);
	}

	@PutMapping("/users/enable-disable")
	public ResponseEntity<?> enableOrDisableUser(@RequestBody ApprovalDeclineDto dto) {
		return dashboardService.enableDisEnabledUser(dto);

	}

	@GetMapping("/users/approvedUser")
	public ResponseEntity<?>  approvedUser(){
		return dashboardService.approvedUser();
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUserById(@PathVariable int id){
		return dashboardService.getUserById(id);
	}

	@PutMapping("/user/update/{id}")
	public ResponseEntity<?> updateUser(@RequestBody UserDtoForUpdate userUpdateDto, @PathVariable int id){
		return dashboardService.updateUser(userUpdateDto,id);
	}

	@DeleteMapping("/delete/user/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable int id){
		return dashboardService.deleteUser(id);
	}


	@GetMapping("/wallet/balance")
	public ResponseEntity<?>  getWalletBalance() throws JsonProcessingException {
		return dashboardService.getWalletBalance();
	}

	@GetMapping("/wallet/collection")
	public ResponseEntity<?> getWalletCollections() throws JsonProcessingException {
		return dashboardService.getWalletCollections();
	}
}
