package com.appbackend.example.AppBackend.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appbackend.example.AppBackend.entities.KycCalculationDetails;
import com.appbackend.example.AppBackend.entities.LoanEligibility;
import com.appbackend.example.AppBackend.repositories.KycCalculationDetailsRepository;
import com.appbackend.example.AppBackend.repositories.LoanEligibilityRepository;
import com.appbackend.example.AppBackend.services.KycCalculationDetailService;

@Service
public class KycCalculationDetailsServiceImpl implements KycCalculationDetailService {
	
	
	@Autowired
	private KycCalculationDetailsRepository kycCalculationDetailsRepository;
	
	
	@Autowired
	private LoanEligibilityRepository loanEligibilityRepository;

	@Override
	public Map<String, Object> getAllCreditInfoGroupedByWorkplace() {
	    List<KycCalculationDetails> allDetails = kycCalculationDetailsRepository.findAll();
	    
	    List<LoanEligibility> allLoanEligibility = loanEligibilityRepository.findAll();
	    
	    Map<String, List<KycCalculationDetails>> groupedByWorkplace = allDetails.stream()
	            .collect(Collectors.groupingBy(KycCalculationDetails::getWorkPlaceName));

	    Map<String, List<Object>> combinedGroupedByWorkplace = new HashMap<>();

	   

	   

	        // Add LoanEligibility objects to the inner map
	        Map<String, Object> innerMap = new HashMap<>();
	        innerMap.put("KycCalculationDetails", groupedByWorkplace);
	        innerMap.put("LoanEligibility", allLoanEligibility);
	        

	    return innerMap;
	}


}
