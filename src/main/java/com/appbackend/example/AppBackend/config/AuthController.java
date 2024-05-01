package com.appbackend.example.AppBackend.config;

import java.util.Optional;

import com.appbackend.example.AppBackend.services.AuthService;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.RefreshToken;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ChangePassword;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.FpOtpReq;
import com.appbackend.example.AppBackend.models.FpOtpVerify;
import com.appbackend.example.AppBackend.models.JwtRequest;
import com.appbackend.example.AppBackend.models.JwtResponse;
import com.appbackend.example.AppBackend.models.OtpRequest;
import com.appbackend.example.AppBackend.models.RefreshTokenRequest;
import com.appbackend.example.AppBackend.models.RegisterRequest;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.RefreshTokenRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.security.JwtHelper;
import com.appbackend.example.AppBackend.services.OtpService;
import com.appbackend.example.AppBackend.services.RefreshTokenService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.utils.ImageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/auth")
@Log
public class AuthController {

    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private KYCRepository kycRepository;


    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService emailOtpService;


    @Autowired
    JwtHelper jwtHelper;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    private Authentication authentication;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request) {
            log.info("Inside Login Controller");
            return authService.login(request);
    }


    @Async
    @PostMapping("/verifyotp")
    public ResponseEntity<?> verifyUserOtp(@RequestBody OtpRequest otpRequest, @CurrentSecurityContext SecurityContext context) throws Exception {
            log.info("Inside Verify Controller");
            return authService.verifyUserOtp(otpRequest);

    }


    @PostMapping("/forgotPasswordOtp")
    public ResponseEntity<?> forgotPassword(@RequestBody FpOtpReq fpOtpReq) {
            log.info("Inside ForgotPasswordOtp Controller");
            return authService.forgotPasswordOtp(fpOtpReq);
    }

    @PostMapping("/forgotPasswordOtpVerify")
    public ResponseEntity<?> forgotPasswordOtpVerify(@RequestBody FpOtpVerify fpOtpVerify) {
            log.info("Inside ForgotPasswordOtpVerify Controller");
            return authService.forgotPasswordOtpVerify(fpOtpVerify);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) {
            log.info("Inside changePassword Controller");
            return authService.changePassword(changePassword);
        }



    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> register(@RequestParam(value = "registerRequest", required = true) String registerRequestString,
                                      @RequestParam(value = "documentData", required = false) MultipartFile documentData) throws Exception {

        return authService.register(registerRequestString, documentData);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshJWTtoken(@RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshTokenString());
            User user = refreshToken.getUser();
            JwtResponse jwtResponse = JwtResponse.builder().refreshTokenString(refreshToken.getRefreshTokenString()).jwtToken(jwtHelper.generateToken(user)).username(user.getUsername()).build();
            return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
        } catch (Exception e) {

            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.UNAUTHORIZED.value()).status("ERROR").message("REFRESH TOKEN HAS BEEN EXPIRED").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);

        }


    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity exceptionHandler() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDto.builder().code(HttpStatus.UNAUTHORIZED.value()).message("CREDENTIALS ARE INVALID").status("ERROR").build());
    }


}
