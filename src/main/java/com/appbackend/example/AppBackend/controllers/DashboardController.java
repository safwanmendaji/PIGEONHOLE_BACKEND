package com.appbackend.example.AppBackend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.UserKYCDto;
import com.appbackend.example.AppBackend.models.PaginationModel.Filter;
import com.appbackend.example.AppBackend.models.PaginationModel.SortField;
import com.appbackend.example.AppBackend.services.DashBoardService;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;
import com.appbackend.example.AppBackend.services.AdminServices.UserDataService;
import com.appbackend.example.AppBackend.util.PIGEONResponse;

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
	public PIGEONResponse getAllUsers() {
		return dashboardService.getAllUsers();
	}

	@GetMapping("/user/{id}")
	public PIGEONResponse getAllUserById(@PathVariable int id) {
		return dashboardService.getUserAndKYCByUserId(id);
	}

	@PostMapping("/calculate_creditscore")
	public ResponseEntity<?> calculateCreditScore(@RequestBody Map<String, Map<String, Object>> objectMap) {
		creditScoreService.calculateCreditScore(objectMap);
		return null;

	}

	@PostMapping("/kyc/update/{id}")
	public PIGEONResponse updateKyc(@RequestBody UserKYCDto userKycDto ,@PathVariable int id) {
		return dashboardService.updateUserKyc(userKycDto,id);
	}


}
