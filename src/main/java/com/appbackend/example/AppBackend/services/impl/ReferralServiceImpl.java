package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;

import com.appbackend.example.AppBackend.entities.ReferralInfo;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ReferDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.repositories.ReferralRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.OtpService;
import com.appbackend.example.AppBackend.services.ReferralService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Autowired
    private OtpService emailOtpService;

    @Autowired
    private AppCommon appCommon;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReferralRepository referralRepository;

    @Override
    public ResponseEntity<?> referralFriend(ReferDto referDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        Optional<User> optionalUser = userRepository.findById(userId);


        String email = referDto.getEmail();
        String mobile = referDto.getMobile();

        try {
            Optional<ReferralInfo> existingReferralByEmail = referralRepository.findByReferalEmail(email);
            Optional<ReferralInfo> existingReferralByPhone = referralRepository.findByReferalMobile(mobile);

            if (existingReferralByEmail.isPresent()) {
                int referringUserIdByEmail = existingReferralByEmail.get().getUserId();
                String referringUserFirstNameByEmail = optionalUser.get().getFirstName();
                return ResponseEntity.badRequest().body("The email " + email + " is already referred by user " + referringUserFirstNameByEmail);
            }

            if (existingReferralByPhone.isPresent()) {
                int referringUserIdByPhone = existingReferralByPhone.get().getUserId();
                String referringUserFirstNameByPhone = optionalUser.get().getFirstName();
                return ResponseEntity.badRequest().body("The mobile number " + mobile + " is already referred by user " + referringUserFirstNameByPhone);
            }


            String combinedData = email + "|" + mobile;
            String referalCode = String.valueOf(appCommon.generateUniqueCode());
            emailOtpService.sendReferCodeViaEmail(email, referalCode);

            ReferralInfo referralInfo = new ReferralInfo();
            referralInfo.setReferalMobile(mobile);
            referralInfo.setReferalEmail(email);
            referralInfo.setUserId(userId);
            referralInfo.setDateTime(LocalDateTime.now());
            referralInfo.setReferralCode(referalCode);

            referralRepository.save(referralInfo);

            SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("Referral Code Sent Successfully")
                    .message("Referral Code Sent Successfully.").build();
            return ResponseEntity.status(HttpStatus.OK).body(successDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL SERVER ERROR");
        }
    }


//    @Override
//    public ResponseEntity<?> verifyReferralCode(ReferDto referDto) {
//        try {
//            String referralCode = referDto.getReferalCode();
//            String mobileNo = referDto.getMobile();
//            String email = referDto.getEmail();
//
//
//            boolean isValid = verifyReferralCode(referralCode, mobileNo, email);
//
//
//            if (!isValid) {
//                SuccessDto successDto = SuccessDto.builder()
//                        .code(HttpStatus.BAD_REQUEST.value())
//                        .status("Invalid referral code, mobile number, or email")
//                        .message("Invalid referral code, mobile number, or email")
//                        .build();
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(successDto);
//            }
//
//
//            String decryptedReferCode = appCommon.decrypt(referDto.getReferenceCode());
//            String[] parts = decryptedReferCode.split("\\|");
//            if (parts.length == 2) {
//                String decryptedEmail = parts[0];
//                String decryptedMobile = parts[1];
//                if (decryptedEmail.equals(referDto.getEmail()) && decryptedMobile.equals(referDto.getMobile())) {
//                    SuccessDto successDto = SuccessDto.builder().code(HttpStatus.OK.value()).status("Referral Code Verify Successfully")
//                            .message("Referral Code Verify Successfully.").build();
//                    return ResponseEntity.status(HttpStatus.OK).body(successDto);
//                } else {
//                    SuccessDto successDto = SuccessDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("Invalid format of reference code")
//                            .message("Invalid format of reference code").build();
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(successDto);
//                }
//            } else {
//                SuccessDto successDto = SuccessDto.builder().code(HttpStatus.BAD_REQUEST.value()).status("Referral Code is Invalid")
//                        .message("Referral Code is Invalid").build();
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(successDto);
//
//            }
//        }
//        } catch (Exception e) {
//            e.printStackTrace();
//            SuccessDto successDto = SuccessDto.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).status("INTERNAL SERVER ERROR")
//                    .message("INTERNAL SERVER ERROR").build();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(successDto);
//        }
//    }

    @Override
    public ResponseEntity<?> verifyReferralCode(ReferDto referDto) {
        try {
            String referralCode = referDto.getReferalCode();
            String mobileNo = referDto.getMobile();
            String email = referDto.getEmail();

            Optional<ReferralInfo> referralInfoOptional = referralRepository.findByReferralCode(referralCode);
            if (!referralInfoOptional.isPresent()) {
                SuccessDto errorDto = SuccessDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("Invalid Referral code ")
                        .message("Invalid Referral code ")
                        .build();
                return ResponseEntity.badRequest().body(errorDto);
            }

            ReferralInfo referralInfo = referralInfoOptional.get();

            if (email.equalsIgnoreCase(referralInfo.getReferalEmail()) && mobileNo.equalsIgnoreCase(referralInfo.getReferalMobile()) && referralCode.equalsIgnoreCase(referralInfo.getReferralCode())) {
                SuccessDto successDto = SuccessDto.builder()
                        .code(HttpStatus.OK.value())
                        .status("Referral Code Verified Successfully")
                        .message("Referral Code Verified Successfully.")
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(successDto);
            } else {

                SuccessDto errorDto = SuccessDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("Invalid referral code, mobile number, or email")
                        .message("Invalid referral code, mobile number, or email")
                        .build();
                return ResponseEntity.badRequest().body(errorDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SuccessDto errorDto = SuccessDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("INTERNAL SERVER ERROR")
                    .message("An unexpected error occurred")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }



}
