package com.appbackend.example.AppBackend.config;

import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.RefreshToken;
import com.appbackend.example.AppBackend.entities.Role;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.*;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.RefreshTokenRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.OtpService;
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
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

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

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody JwtRequest request) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserName());
			User user = (User) userDetails;

			if (user.getRole().equals(Role.USER) && user.getIsApproved() != null && !user.getIsApproved()) {
				ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.FORBIDDEN.value()).status("ERROR")
						.message("YOUR PROFILE IS NOT APPROVED BY ADMIN").build();
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
			}

			this.doAuthenticate(request.getUserName(), request.getPassword());
			emailOtpService.sendVerificationOtp(user, request.getUserName());

			SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("SUCCESS")
					.message("OTP has been sent to " + request.getUserName()).build();
			return ResponseEntity.status(HttpStatus.OK).body(successDto);
		} catch (UsernameNotFoundException e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR")
					.message("User with email " + request.getUserName() + " is not found.").build();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
		}
	}

	@Async
	@PostMapping("/verifyotp")
	public ResponseEntity<?> verifyUserOtp(@RequestBody OtpRequest otpRequest,
			@CurrentSecurityContext SecurityContext context) {
		try {
			UserDetails userDetails = null;
			User user = null;

			if (otpRequest.getEmail().contains("@")) {
				userDetails = userDetailsService.loadUserByUsername(otpRequest.getEmail());
				user = userRepository.findByEmail(otpRequest.getEmail()).orElseThrow(
						() -> new UsernameNotFoundException("USER NOT FOUND FOR EMAIL: " + otpRequest.getEmail()));
			} else {
				userDetails = userDetailsService.loadUserByUsername(otpRequest.getEmail());
				user = userRepository.findByPhoneNumber(otpRequest.getEmail())
						.orElseThrow(() -> new UsernameNotFoundException(
								"USER NOT  FOUND FOR PHONE NUMBER: " + otpRequest.getEmail()));
			}

			emailOtpService.verifyOtp(otpRequest.getOtp(), user);

			RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
			String token = jwtHelper.generateToken(userDetails);

			JwtResponse response = JwtResponse.builder().jwtToken(token)
					.refreshTokenString(refreshToken.getRefreshTokenString()).username(userDetails.getUsername())
					.build();

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (UsernameNotFoundException e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.NOT_FOUND.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
		}
	}

	@PostMapping("/forgotPasswordOtp")
	public ResponseEntity<?> forgotPassword(@RequestBody FpOtpReq fpOtpReq) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(fpOtpReq.getEmail());
			User user = userRepository.findByEmail(fpOtpReq.getEmail()).get();

			emailOtpService.sendVerificationOtp((User) userDetails, fpOtpReq.getEmail());

			String response = "OTP HAS BEEN SEND TO " + " " + user.getEmail();
			SuccessDto successDto = SuccessDto.builder().message(response).code(HttpStatus.OK.value()).status("SUCCESS")
					.build();
			return ResponseEntity.status(HttpStatus.OK).body(successDto);

		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
		}

	}

	@PostMapping("/forgotPasswordOtpVerify")
	public ResponseEntity<?> forgotPasswordOtpVerify(@RequestBody FpOtpVerify fpOtpVerify) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(fpOtpVerify.getEmail());
			User user = userRepository.findByEmail(fpOtpVerify.getEmail()).get();

			String response = emailOtpService.verifyFpwOtp(fpOtpVerify.getOtp(), user);

			System.out.println(response);

			SuccessDto successDto = SuccessDto.builder().status("SUCCESS").message(response).code(HttpStatus.OK.value())
					.build();

			return ResponseEntity.status(HttpStatus.OK).body(successDto);

		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
		}

	}

	@PostMapping("/changePassword")
	public ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(changePassword.getEmail());
			User user = userRepository.findByEmail(changePassword.getEmail()).get();

			user.setPassword(passwordEncoder.encode(changePassword.getPassword()));

			userRepository.save(user);

			SuccessDto successDto = SuccessDto.builder().status("SUCCESS")
					.message("YOUR PASSWORD HAS BEEN CHANGED SUCCESSFULLY").code(HttpStatus.OK.value()).build();

			return ResponseEntity.status(HttpStatus.OK).body(successDto);

		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
		}

	}

	@PostMapping(value = "/register", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> register(
			@RequestParam(value = "registerRequest", required = true) String registerRequestString,
			@RequestParam(value = "documentData", required = false) MultipartFile documentData)
			throws JsonProcessingException {

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
			Optional<User> duplicatePhoneUser = userService.getUserByPhone(registerRequest.getEmail());

			Optional<User> duplicateIdUser = userService.getUserById(registerRequest.getId());

			if (duplicateEmailUser.isPresent()) {
				throw new DuplicateUserException("USER WITH THIS EMAIL ID ALREADY EXISTS");
			}

			if (duplicateIdUser.isPresent()) {
				throw new DuplicateUserException("USER WITH THIS  ID ALREADY EXISTS");
			}

			if (duplicatePhoneUser.isPresent()) {
				throw new DuplicateUserException("USER WITH THIS MOBILE ALREADY EXISTS");
			}

			User user = User.builder().id(registerRequest.getId()).firstName(registerRequest.getFirstname())
					.lastName(registerRequest.getLastname()).email(registerRequest.getEmail())
					.password(passwordEncoder.encode(registerRequest.getPassword()))
					.phoneNumber(registerRequest.getPhoneNumber()).build();

			user.setRoleByInput(registerRequest.getRole());
			user.setIsApproved(false);
				userRepository.save(user);

			if (registerRequest.getRole() == 2) {

				if (documentData == null) {
					throw new Exception("WORK ID DOCUMENT IS REQUIRED KINDLY UPLOAD IT");
				} else {
					KYC kyc = KYC.builder().user(user).id(user.getId())
							.documentData(ImageUtils.compressImage(documentData.getBytes())).build();

					kycRepository.save(kyc);

				}
			}

			SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("success")
					.message("USER  HAVE BEEN SUCCESSFULLY REGISTERED").build();

			return ResponseEntity.status(HttpStatus.OK).body(successDto);

		} catch (DuplicateUserException e) {

			String errorResponse = e.getMessage();
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.CONFLICT.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);

		} catch (Exception e) {
			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("ERROR")
					.message(e.getMessage()).build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
		}

	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshJWTtoken(@RequestBody RefreshTokenRequest request) {

		try {
			RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshTokenString());

			User user = refreshToken.getUser();

			JwtResponse jwtResponse = JwtResponse.builder().refreshTokenString(refreshToken.getRefreshTokenString())
					.jwtToken(jwtHelper.generateToken(user)).username(user.getUsername()).build();

			return new ResponseEntity<>(jwtResponse, HttpStatus.OK);

		} catch (Exception e) {

			ErrorDto errorDto = ErrorDto.builder().code(HttpStatus.UNAUTHORIZED.value()).status("ERROR")
					.message("REFRESH TOKEN HAS BEEN EXPIRED").build();
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

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDto.builder()
				.code(HttpStatus.UNAUTHORIZED.value()).message("CREDENTIALS ARE INVALID").status("ERROR").build());
	}

}
