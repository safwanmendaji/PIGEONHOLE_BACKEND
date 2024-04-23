package com.appbackend.example.AppBackend.services;


import com.appbackend.example.AppBackend.models.UserKYCDto;
import org.springframework.http.ResponseEntity;

public interface DashBoardService {

	ResponseEntity<?> getAllUsers();



	ResponseEntity<?> getUserAndKYCByUserId(int id);



	ResponseEntity<?> updateUserKyc(UserKYCDto userKycDto);






	ResponseEntity<?> enableDisEnabledUser(int id);


}
