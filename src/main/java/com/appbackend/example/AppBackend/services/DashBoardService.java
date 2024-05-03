package com.appbackend.example.AppBackend.services;


import com.appbackend.example.AppBackend.models.ApprovalDeclineDto;
import com.appbackend.example.AppBackend.models.UserKYCDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface DashBoardService {

	ResponseEntity<?> getAllUsers();



	ResponseEntity<?> getUserAndKYCByUserId(int id);



	ResponseEntity<?> updateUserKyc(UserKYCDto userKycDto);



	ResponseEntity<?> enableDisEnabledUser(ApprovalDeclineDto dto);

	ResponseEntity<?> approvedUser();


}
