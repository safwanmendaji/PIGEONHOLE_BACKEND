package com.appbackend.example.AppBackend.config;

import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.RefreshToken;
import com.appbackend.example.AppBackend.entities.Role;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.RefreshTokenRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.EmailOtpService;
import com.appbackend.example.AppBackend.services.RefreshTokenService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.utils.ImageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appbackend.example.AppBackend.security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/auth")
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


//    @Autowired
//    private RegisterRequest registerRequest;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailOtpService emailOtpService;


    @Autowired
    JwtHelper jwtHelper;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    private Authentication authentication;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request) {
        try {


            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            this.doAuthenticate(request.getEmail(), request.getPassword());

//        Here saveOtp method  of emailOtpService will return an otp string

            String otp = emailOtpService.saveOtp((User) userDetails);
            emailOtpService.sendVerificationOtpEmail(userDetails.getUsername(), otp);


//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
//
//
//        String token = this.jwtHelper.generateToken(userDetails);
//
//
//        JwtResponse response = JwtResponse.builder().jwtToken(token).refreshTokenString(refreshToken.getRefreshTokenString()).username(userDetails.getUsername()).build();


//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Object user =this.authentication.getName();

//        System.out.println(user);

            SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("SUCCESS").message("OTP HAS BEEN SEND TO " + authentication.getName()).build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (UsernameNotFoundException e) {

            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("USER WITH EMAIL " + request.getEmail() + " IS NOT FOUND ").build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        }
    }


    @Async
    @PostMapping("/verifyotp")
    public ResponseEntity<?> verifyUserOtp(@RequestBody OtpRequest otpRequest, @CurrentSecurityContext SecurityContext context) {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

//            Principal principal ;
//
//            System.out.println(principal.getName());
//            System.out.println("This is ");
//            System.out.println(context.getAuthentication().getName());
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(otpRequest.getEmail());
            User user = userRepository.findByEmail(otpRequest.getEmail()).get();


            emailOtpService.verifyOtp(otpRequest.otp, user);

            System.out.println(emailOtpService.verifyOtp(otpRequest.otp, user));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());


//            CompletableFuture<String> jwtfuture = CompletableFuture.supplyAsync(() -> {
//                System.out.println("generating jwt");
//                return this.jwtHelper.generateToken(userDetails);
//            });
//
//            ResponseEntity<?> responseEntity = jwtfuture.thenApply(jwt -> {
//                JwtResponse response1 = JwtResponse.builder().jwtToken(jwt)
//                        .refreshTokenString(refreshToken.getRefreshTokenString())
//                        .username(userDetails.getUsername())
//                        .build();
//
//                System.out.println("Sending JWT: " + jwt);
//                return new ResponseEntity<>(response1, HttpStatus.OK);
//            }).exceptionally(error -> {
//                System.out.println("Error generating JWT: " + error.getMessage());
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }).join();
//
//            CompletableFuture.runAsync(() -> {
//
//                long startTime = System.currentTimeMillis();
//                long endTime = startTime + 10000; // Delay for 10 second
//                while (System.currentTimeMillis() < endTime) {
//                    // Wait for 1 second
//                }
//                System.out.println("Hello World!");
//
//            });

            String token = this.jwtHelper.generateToken(userDetails);


            JwtResponse response = JwtResponse.builder().jwtToken(token).refreshTokenString(refreshToken.getRefreshTokenString()).username(userDetails.getUsername()).build();


            return new ResponseEntity<>(response, HttpStatus.OK);
//            return responseEntity;


//            return  new ResponseEntity<>("You are successfully logged in",HttpStatus.OK);
        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        }


    }

    {

    }

    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> register(@RequestParam(value = "registerRequest", required = true) String registerRequestString, @RequestParam(value = "documentData", required = false) MultipartFile documentData) throws JsonProcessingException {


//        System.out.println("DOC DATA IS HERE ------>  "+documentData);

        ObjectMapper mapper = new ObjectMapper();
        RegisterRequest registerRequest = mapper.readValue(registerRequestString, RegisterRequest.class);


        try {


            if (registerRequest.getRole() < 1 && registerRequest.getRole() > 2) {
                throw new Exception("Invalid input. Only 1 (ADMIN) or 2 (USER) are allowed.");
            }

            if (registerRequest.getEmail().trim().isEmpty()) {
                throw new Exception("Email Field must not be null");
            }

            if (registerRequest.getId() == null) {
                throw new Exception("ID Field must not be null");
            }

            Optional<User> duplicateEmailUser = userService.getUserByEmail(registerRequest.getEmail());

            Optional<User> duplicateIdUser = userService.getUserById(registerRequest.getId());

            if (duplicateEmailUser.isPresent()) {
                throw new DuplicateUserException("USER WITH THIS EMAIL ID ALREADY EXISTS");
            }

            if (duplicateIdUser.isPresent()) {
                throw new DuplicateUserException("USER WITH THIS  ID ALREADY EXISTS");
            }


            User user = User.builder().id(registerRequest.getId()).firstName(registerRequest.getFirstname()).lastName(registerRequest.getLastname()).email(registerRequest.getEmail()).password(passwordEncoder.encode(registerRequest.getPassword())).phoneNumber(registerRequest.getPhoneNumber()).build();

            user.setRoleByInput(registerRequest.getRole());

            userRepository.save(user);

            if (registerRequest.getRole() == 2) {

                if (documentData == null) {
                    throw new Exception("WORK ID DOCUMENT IS REQUIRED KINDLY UPLOAD IT");
                } else {
                    KYC kyc = KYC.builder().user(user).id(user.getId()).documentData(ImageUtils.compressImage(documentData.getBytes())).build();

                    kycRepository.save(kyc);

                }
            }





//
//            var token = jwtHelper.generateToken(user);

            SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success").message("USER  HAVE BEEN SUCCESSFULLY REGISTERED").build();


            return ResponseEntity.status(HttpStatus.OK).body(successDto);


        } catch (DuplicateUserException e) {

            String errorResponse = e.getMessage();
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.CONFLICT.value()).status("ERROR").message(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);

        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        }


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


    public void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            this.authentication = authentication;

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID USERNAME OR PASSWORD");

        }


    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity exceptionHandler() {


        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDto.builder().code(HttpStatus.UNAUTHORIZED.value()).message("CREDENTIALS ARE INVALID").status("ERROR").build());
    }


}
