package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest) throws Exception;

    ResponseEntity<?> login(JwtRequest request);

    ResponseEntity<?> forgotPasswordOtp(FpOtpReq fpOtpReq);

    ResponseEntity<?> forgotPasswordOtpVerify(FpOtpVerify fpOtpVerify);

    ResponseEntity<?> changePassword(ChangePassword changePassword);

    ResponseEntity<?> register(String registerRequestString, MultipartFile documentData) throws Exception;
}
