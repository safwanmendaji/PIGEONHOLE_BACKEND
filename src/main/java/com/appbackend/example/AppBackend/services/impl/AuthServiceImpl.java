package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.config.DuplicateUserException;
import com.appbackend.example.AppBackend.config.OtpExpiredException;
import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.RefreshToken;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.security.JwtHelper;
import com.appbackend.example.AppBackend.services.AuthService;
import com.appbackend.example.AppBackend.services.OtpService;
import com.appbackend.example.AppBackend.services.RefreshTokenService;
import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.utils.ImageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Log
public class AuthServiceImpl implements AuthService {

    @Autowired
    private OtpService emailOtpService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtHelper jwtHelper;

    private Authentication authentication;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KYCRepository kycRepository;
    @Autowired
    private UserService userService;


    @Override
    public ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest) {
        try {
            log.info("Inside AuthServiceImpl verifyUserOtp Method");
            UserDetails userDetails = userDetailsService.loadUserByUsername(otpRequest.getEmail());
            User user = userRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify OTP and check expiration
            boolean otpVerified = emailOtpService.verifyOtpAndCheckExpiration(otpRequest.getOtp(), user);

            if (otpVerified) {
                // If OTP verification is successful, generate JWT token and refresh token
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
                String token = jwtHelper.generateToken(userDetails);
                JwtResponse response = JwtResponse.builder().jwtToken(token).refreshTokenString(refreshToken.getRefreshTokenString()).username(userDetails.getUsername()).build();
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                // If OTP verification fails or it has expired
                ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message("Incorrect or expired OTP").build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            }
        } catch (UsernameNotFoundException e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("User not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("Internal server error").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }



    @Override
    public ResponseEntity<?> login(JwtRequest request) {
        try {
            log.info("Inside AuthServiceImpl login method");
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserName());
            System.out.println("Name==>> " + userDetails.getUsername());

            this.doAuthenticate(request.getUserName(), request.getPassword());
            emailOtpService.sendVerificationOtp((User) userDetails, request.getUserName());

            SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("SUCCESS").message("OTP HAS BEEN SEND TO " + authentication.getName()).build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (UsernameNotFoundException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("User not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        }
        catch (BadCredentialsException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message("CREDENTIALS WRONG").build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        }
        catch(Exception e){
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("SOMETHING WENT WRONG").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }

    }

    @Override
    public ResponseEntity<?> forgotPasswordOtp(FpOtpReq fpOtpReq) {
        try {
            log.info("Inside AuthServiceImpl forgotPasswordOtp method");
            UserDetails userDetails = userDetailsService.loadUserByUsername(fpOtpReq.getEmail());
            User user = userRepository.findByEmail(fpOtpReq.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            emailOtpService.sendVerificationOtp(user, fpOtpReq.getEmail());
            String response = "OTP HAS BEEN SEND TO " + user.getEmail();
            SuccessDto successDto = SuccessDto.builder().message(response).code(HttpStatus.OK.value()).status("SUCCESS").build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (UsernameNotFoundException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("User not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);

        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("Internal server error").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }


    @Override
    public ResponseEntity<?> forgotPasswordOtpVerify(FpOtpVerify fpOtpVerify) {
        try {
            log.info("Inside AuthServiceImpl forgotPasswordOtpVerify method");
            UserDetails userDetails = userDetailsService.loadUserByUsername(fpOtpVerify.getEmail());
            User user = userRepository.findByEmail(fpOtpVerify.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify OTP
            emailOtpService.verifyFpwOtp(fpOtpVerify.getOtp(), user);

            SuccessDto successDto = SuccessDto.builder().status("SUCCESS").message("OTP verification successful now you can change password").code(HttpStatus.OK.value()).build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (UsernameNotFoundException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("User not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        } catch (IllegalArgumentException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message(ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("Internal server error").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }



    @Override
    public ResponseEntity<?> changePassword(ChangePassword changePassword) {
        try {
            log.info("Inside AuthServiceImpl changePassword method");
            if (changePassword.getPassword().isEmpty()) {
                ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR").message("Password must not be empty").build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(changePassword.getEmail());
            User user = userRepository.findByEmail(changePassword.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            user.setPassword(passwordEncoder.encode(changePassword.getPassword()));
            userRepository.save(user);
            SuccessDto successDto = SuccessDto.builder().status("SUCCESS").message("YOUR PASSWORD HAS BEEN CHANGED SUCCESSFULLY").code(HttpStatus.OK.value()).build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (UsernameNotFoundException ex) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR").message("User not found").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("ERROR").message("Internal server error").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }



    @Override
    public ResponseEntity<?> register(String registerRequestString, MultipartFile documentData) {
        try {
            log.info("Inside AuthServiceImpl register method");
            ObjectMapper mapper = new ObjectMapper();
            RegisterRequest registerRequest = mapper.readValue(registerRequestString, RegisterRequest.class);

            log.info("Inside register Controller");

            // Check if email field is empty
            if (registerRequest.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email Field must not be null");
            }

            // Check if email is already in use
            Optional<User> duplicateEmailUser = userService.getUserByEmail(registerRequest.getEmail());
            if (duplicateEmailUser.isPresent()) {
                throw new DuplicateUserException("User with this email already exists");
            }

            // Check if phone number is already in use
            Optional<User> duplicatePhoneUser = userService.getUserByPhone(registerRequest.getPhoneNumber());
            if (duplicatePhoneUser.isPresent()) {
                throw new DuplicateUserException("User with this phone number already exists");
            }

            // Role validation
            if (registerRequest.getRole() < 1 || registerRequest.getRole() > 2) {
                throw new IllegalArgumentException("Invalid input. Only 1 (ADMIN) or 2 (USER) are allowed.");
            }

            // Create new user
            User user = User.builder()
                    .firstName(registerRequest.getFirstname())
                    .lastName(registerRequest.getLastname())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .build();
            user.setRoleByInput(registerRequest.getRole());
            userRepository.save(user);

            // Save KYC if role is USER and documentData is provided
            if (registerRequest.getRole() == 2 && documentData != null) {
                KYC kyc = KYC.builder()
                        .user(user)
                        .id(user.getId())
                        .documentData(ImageUtils.compressImage(documentData.getBytes()))
                        .build();
                kycRepository.save(kyc);
            }

            // Return success response
            SuccessDto successDto = SuccessDto.builder()
                    .code(HttpStatus.OK.value())
                    .status("success")
                    .message("USER HAS BEEN SUCCESSFULLY REGISTERED")
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (DuplicateUserException ex) {
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status("error")
                    .message(ex.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        } catch (IllegalArgumentException ex) {
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status("error")
                    .message(ex.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
        } catch (Exception e) {
            ErrorDto errorDto = ErrorDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("error")
                    .message("Internal server error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
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

}