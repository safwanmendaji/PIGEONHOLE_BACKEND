package com.appbackend.example.AppBackend.services;


import com.appbackend.example.AppBackend.entities.CreditScore;
import com.appbackend.example.AppBackend.entities.KYC;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.entities.UtilizeUserCredit;
import com.appbackend.example.AppBackend.enums.KycStatus;
import com.appbackend.example.AppBackend.models.KYCDataResDto;
//import com.appbackend.example.AppBackend.models.KYCDto;

import com.appbackend.example.AppBackend.models.KYCDocData;
import com.appbackend.example.AppBackend.models.RegisterRequest;
import com.appbackend.example.AppBackend.repositories.CreditScoreRepository;
import com.appbackend.example.AppBackend.repositories.KYCRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.repositories.UtilizeUserCreditRepository;
import com.appbackend.example.AppBackend.services.AdminServices.CreditScoreService;
import com.appbackend.example.AppBackend.services.impl.StorageService;
import com.appbackend.example.AppBackend.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KYCService {


    @Autowired
    private KYCRepository kycRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditScoreService creditScoreService;

    @Autowired
    CreditScoreRepository creditScoreRepository;

    @Autowired
    private UtilizeUserCreditRepository utilizeUserCreditRepository;

    @Autowired
    private StorageService storageService;

//    @Transactional
//    public KYCDataResDto saveUserKYC(KYCDataResDto kycRequest, MultipartFile documentData, MultipartFile userImage, MultipartFile digitalSignature) throws IOException {
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) authentication.getPrincipal();
//        String ageFromKYCRequest = calculateAge(kycRequest.getDob());
//
//
//        KYC existingKyc = kycRepository.findKYCById((((User) authentication.getPrincipal()).getId()));
//
//
//        if (existingKyc != null) {
//
//            if (kycRequest.getDob() != null &&  existingKyc.getDob() == null) {
//                existingKyc.setDob(kycRequest.getDob());
//
//            }
//            if(kycRequest.getPhoneNumber() != null && existingKyc.getUser() != null
//                    && existingKyc.getUser().getPhoneNumber() == null){
//                existingKyc.getUser().setPhoneNumber(kycRequest.getPhoneNumber());
//            }
//            if (kycRequest.getAddress() != null && existingKyc.getAddress() == null) {
//                existingKyc.setAddress(kycRequest.getAddress());
//            }
//            if (kycRequest.getMaritalStatus() != null && existingKyc.getMaritalStatus() == null) {
//                existingKyc.setMaritalStatus(kycRequest.getMaritalStatus());
//            }
//            if (kycRequest.getKin() != null && existingKyc.getKin() == null) {
//                existingKyc.setKin(kycRequest.getKin());
//            }
//            if (kycRequest.getKinNumber() != null && existingKyc.getKinNumber() == null) {
//                existingKyc.setKinNumber(kycRequest.getKinNumber());
//            }
//            if (kycRequest.getKin1() != null && existingKyc.getKin1() == null) {
//                existingKyc.setKin1(kycRequest.getKin1());
//            }
//            if (kycRequest.getKin1Number() != null && existingKyc.getKin1Number() == null) {
//                existingKyc.setKin1Number(kycRequest.getKin1Number());
//            }
//            if (kycRequest.getNationalId() != null && existingKyc.getNationalId() == null) {
//                existingKyc.setNationalId(kycRequest.getNationalId());
//            }
//            if (kycRequest.getGender() != null && existingKyc.getGender() == null) {
//                existingKyc.setGender(kycRequest.getGender());
//            }
//
//            if (existingKyc.getAge() == null) {
//                String age = ageFromKYCRequest;
//            }
//            if (documentData != null && existingKyc.getDocumentData() == null) {
//                existingKyc.setDocumentData(ImageUtils.compressImage(documentData.getBytes()));
//            }
//            if (digitalSignature != null && existingKyc.getDigitalSignature() == null) {
//                existingKyc.setDigitalSignature(ImageUtils.compressImage(digitalSignature.getBytes()));
//            }
//
//            if (userImage != null && existingKyc.getUserImage() == null) {
//                String userImageUrl = storageService.uploadFileToS3(userImage);
//                existingKyc.setUserImage(userImageUrl);
//            }
//
//            existingKyc.setStatus(String.valueOf(KycStatus.PENDING));
//
//
//
//            System.out.println(calculateAge(kycRequest.getDob()));
//
//            CreditScore creditScore = getCreditScore(kycRequest, user, ageFromKYCRequest);
//
//
//            kycRepository.save(existingKyc);
//
//            assert documentData != null;
//            assert userImage != null;
//            assert digitalSignature != null;
//            KYCDataResDto kycResponse = KYCDataResDto.builder()
//                    .workId(existingKyc.getId())
//                    .dob(existingKyc.getDob())
//                    .phoneNumber(existingKyc.getUser().getPhoneNumber())
//                    .firstName(existingKyc.getUser().getFirstName())
//                    .lastName(existingKyc.getUser().getLastName())
//                    .nationalId(existingKyc.getNationalId())
//                    .age(existingKyc.getAge())
//                    .kin(existingKyc.getKin())
//                    .kinNumber(existingKyc.getKinNumber())
//                    .kin1(existingKyc.getKin1())
//                    .kin1Number(existingKyc.getKin1Number())
//                    .address(existingKyc.getAddress())
//                    .email(existingKyc.getUser().getUsername())
//                    .gender(existingKyc.getGender())
//                    .isDocumentDataSubmitted(existingKyc.getDocumentData() != null)
//                    .isDigitalSignatureSubmitted(existingKyc.getDigitalSignature() != null)
//                    .isUserImageSubmitted(existingKyc.getUserImage() != null)
//                    .build();
//            return kycResponse;
//
//        }
//
//        assert documentData != null;
//        assert userImage != null;
//        assert digitalSignature != null;
//
//
////            if user is first time filling the kyc form then first build kyc object then save in db
//        user.setPhoneNumber(kycRequest.getPhoneNumber());
//        KYC kyc = KYC.builder()
//                .id(user.getId())
//                .user(user)
//                .gender(kycRequest.getGender())
//                .dob(kycRequest.getDob())
//                .age(ageFromKYCRequest)
//                .address(kycRequest.getAddress())
//                .maritalStatus(kycRequest.getMaritalStatus())
//                .kin(kycRequest.getKin())
//                .kinNumber(kycRequest.getKinNumber())
//                .kin1(kycRequest.getKin1())
//                .kin1Number(kycRequest.getKin1Number())
//                .nationalId(kycRequest.getNationalId())
//                .build();
//
//
//        CreditScore creditScore = getCreditScore(kycRequest, user, ageFromKYCRequest);
//
//
//        if (userImage != null) {
//            existingKyc.setUserImage(String.valueOf(userImage));
//        }
//        if (documentData != null) {
//            kyc.setDocumentData(ImageUtils.compressImage(documentData.getBytes()));
//        }
//        if (digitalSignature != null) {
//            kyc.setDigitalSignature(ImageUtils.compressImage(digitalSignature.getBytes()));
//        }
//
////        save kyc in db
//        kycRepository.save(kyc);
//
//
////        build kyc first time response for first time registration
//        KYCDataResDto kycFirstTimeResponse = KYCDataResDto.builder()
//                .workId(user.getId())
//                .firstName(user.getFirstName())
//                .lastName(user.getLastName())
//                .phoneNumber(user.getPhoneNumber())
//                .email(user.getUsername())
//                .age(kyc.getAge())
//                .dob(kyc.getDob())
//                .address(kyc.getAddress())
//                .gender(kyc.getGender())
//                .maritalStatus(kyc.getMaritalStatus())
//                .kin(kyc.getKin())
//                .kinNumber(kyc.getKinNumber())
//                .kin1(kyc.getKin1())
//                .kin1Number(kyc.getKin1Number())
//                .nationalId(kyc.getNationalId())
//                .isDocumentDataSubmitted(kyc.getDocumentData() != null)
//                .isUserImageSubmitted(kyc.getUserImage() != null)
//                .isDigitalSignatureSubmitted(kyc.getDigitalSignature() != null)
//                .build();
//
//        return kycFirstTimeResponse;
//
//
//    }


    @Transactional
    public RegisterRequest saveUserKYC(RegisterRequest registerRequest, String documentData, String userImage, String digitalSignature,User user) throws IOException {
        String ageFromKYCRequest = calculateAge(registerRequest.getDob());

        assert documentData != null;
        assert userImage != null;
        assert digitalSignature != null;


//            if user is first time filling the kyc form then first build kyc object then save in db
        KYC kyc = KYC.builder()
                .id(registerRequest.getId())
                .user(user)
                .gender(registerRequest.getGender())
                .dob(registerRequest.getDob())
                .age(ageFromKYCRequest)
                .address(registerRequest.getAddress())
                .maritalStatus(registerRequest.getMaritalStatus())
                .kin(registerRequest.getKin())
                .kinNumber(registerRequest.getKinNumber())
                .kin1(registerRequest.getKin1())
                .kin1Number(registerRequest.getKin1Number())
                .nationalId(registerRequest.getNationalId())
                .userImage(userImage)
                .digitalSignature(digitalSignature)
                .documentData(documentData)
                .build();


        CreditScore creditScore = getCreditScore(registerRequest, user, ageFromKYCRequest);


//        if (userImage != null) {
//            existingKyc.setUserImage(String.valueOf(userImage));
//        }
//        if (documentData != null) {
//            kyc.setDocumentData(ImageUtils.compressImage(documentData.getBytes()));
//        }
//        if (digitalSignature != null) {
//            kyc.setDigitalSignature(ImageUtils.compressImage(digitalSignature.getBytes()));
//        }

//        save kyc in db
        kycRepository.save(kyc);


//        build kyc first time response for first time registration
        RegisterRequest kycFirstTimeResponse = RegisterRequest.builder()
                .workId(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getUsername())
                .age(kyc.getAge())
                .dob(kyc.getDob())
                .address(kyc.getAddress())
                .gender(kyc.getGender())
                .maritalStatus(kyc.getMaritalStatus())
                .kin(kyc.getKin())
                .kinNumber(kyc.getKinNumber())
                .kin1(kyc.getKin1())
                .kin1Number(kyc.getKin1Number())
                .nationalId(kyc.getNationalId())
                .isDocumentDataSubmitted(kyc.getDocumentData() != null)
                .isUserImageSubmitted(kyc.getUserImage() != null)
                .isDigitalSignatureSubmitted(kyc.getDigitalSignature() != null)
                .build();

        return kycFirstTimeResponse;


    }


    public ResponseEntity<?> getKycUserData(Authentication authentication){
//        User user = (User) authentication.getPrincipal();
//
//
//        return new ResponseEntity<>(kycService.getUserKYCDataById(user.getId(), authentication)
//                .orElseThrow(() -> new RuntimeException("KYC OF USER NOT FOUND")), HttpStatus.OK);
        return null;
    }

    private CreditScore getCreditScore(RegisterRequest kycRequest, User user, String ageFromKYCRequest) {
        int ageCreditsScore = creditScoreService.ageCreditScore(Integer.parseInt(ageFromKYCRequest));
        int genderCreditScore = creditScoreService.genderCreditScore(kycRequest.getGender());
        int kinCreditScore = creditScoreService.nextOfKin(kycRequest.getKin());

        double ageCreditScoreCalculated = creditScoreService.calculateCommonCreditScore(ageCreditsScore , 1);
        double genderCreditScoreCalculated = creditScoreService.calculateCommonCreditScore(genderCreditScore , 1);
        double kinCreditScoreCalculated = creditScoreService.calculateCommonCreditScore(kinCreditScore , 5);


        String ageCreditObject = creditScoreService.makeCreditScoreOjb(ageCreditsScore, 1, (float ) ageCreditScoreCalculated,0);
        String genderCreditObject = creditScoreService.makeCreditScoreOjb(genderCreditScore, 1, (float )  genderCreditScoreCalculated,0);
        String kinCreditObject = creditScoreService.makeCreditScoreOjb(kinCreditScore, 5, (float ) kinCreditScoreCalculated,0);



        float totalExposure = (float) (ageCreditScoreCalculated + genderCreditScoreCalculated + kinCreditScoreCalculated);
        Integer totalCreditScore = ageCreditsScore + genderCreditScore + kinCreditScore;
//        int totalCreditScoreValue = totalCreditScore * 5;
//        float averageCreditScoreValue = totalCreditScoreValue / 3;

        CreditScore creditScore = CreditScore.builder()
                .user(user)
                .id(user.getId())
                .age(ageCreditObject)
                .gender(genderCreditObject)
                .nextOfKinType(kinCreditObject)
                .totalExposure(totalExposure)
                .totalCreditScore(totalCreditScore)
//                .totalCreditScoreValue(totalCreditScoreValue)
//                .averageCreditScoreValue(averageCreditScoreValue)
                .build();
        creditScoreRepository.save(creditScore);
        return creditScore;
    }

    private static byte[] docToByte(byte[] document) {
        if (document != null) {

            byte[] imageBytes = ImageUtils.decompressImage(document);
            return imageBytes;
        }
        return null;
    }

    @Transactional
    public Object getUserKYCDocDataById(Integer id) {
        KYC kycData = kycRepository.findKYCById(id);

//        if (kycData != null) {
//            int docSignSize = kycData.getDocumentData() != null ? docToByte(kycData.getDocumentData()).length : 0;
//            int userImageSize = kycData.getUserImage() != null ? docToByte(kycData.getUserImage()).length : 0;
//            int digitalSignSize = kycData.getDigitalSignature() != null ? docToByte(kycData.getDigitalSignature()).length : 0;
//
//            KYCDocData kycDocumentData = new KYCDocData().builder()
//                    .documentData(docToByte(kycData.getDocumentData()))
//                    .userImage(docToByte(kycData.getUserImage()))
//                    .digitalSignature(docToByte(kycData.getDigitalSignature()))
//                    .docSize(docSignSize)
//                    .userImgSize(userImageSize)
//                    .digitalSignSize(digitalSignSize)
//                    .build();
//            return kycDocumentData;
//
//
//        } else {
//            return "KYC data with id " + id + " not found";
//        }

        return null;
    }


    //    The getUserKYCDataById wil only fetch user data not documents or images
    @Transactional
    public Optional<KYCDataResDto> getUserKYCDataById(Integer id, Authentication authentication) {

        //get KYC user details if present kycid exists in KYC db
        if (kycRepository.findKYCById(id) != null) {


            KYC kyc = kycRepository.findKYCById(id);

            KYCDataResDto kycResponse = KYCDataResDto.builder()
                    .email(kyc.getUser().getUsername())
                    .address(kyc.getAddress())
                    .maritalStatus(kyc.getMaritalStatus())
                    .kin(kyc.getKin())
                    .kinNumber(kyc.getKinNumber())
                    .kin1(kyc.getKin1())
                    .kin1Number(kyc.getKin1Number())
                    .nationalId(kyc.getNationalId())
                    .dob(kyc.getDob())
                    .age(kyc.getAge())
                    .gender(kyc.getGender())
                    .phoneNumber(kyc.getUser().getPhoneNumber())
                    .firstName(kyc.getUser().getFirstName())
                    .lastName(kyc.getUser().getLastName())
                    .workId(kyc.getId())
                    .userId(kyc.getUser().getId())

                    .isUserImageSubmitted(kyc.getUserImage() != null)
                    .isDocumentDataSubmitted(kyc.getDocumentData() != null)
                    .isDigitalSignatureSubmitted(kyc.getDigitalSignature() != null)
                    .build();
//
//            Pageable pageable = PageRequest.of(0, 1); // Limiting to 1 result
            UtilizeUserCredit userCredit = utilizeUserCreditRepository.findFirstByUserIdOrderByIdDesc(kyc.getUser().getId());
            if(userCredit != null){
                Map<String , Object> map = new HashMap<>();
                long eligibleAmount = userCredit.getUserLoanEligibility().getEligibilityAmount();
                Double availableAmount =  userCredit.getAvailableBalance();
                double utilizeAmount = eligibleAmount - availableAmount;

                map.put("eligibleAmount" , eligibleAmount);
                map.put("utilizeAmount" , utilizeAmount);
                map.put("availableAmount" , availableAmount);
                kycResponse.setLoanAmountInfo(map);
            }

            return Optional.ofNullable(kycResponse);

        }
        //  else get current user details for form field data binding
        else {

            User kycUser = (User) authentication.getPrincipal();



            KYCDataResDto kycResponse = KYCDataResDto.builder()
                    .firstName(kycUser.getFirstName())
                    .lastName(kycUser.getLastName())
                    .phoneNumber(kycUser.getPhoneNumber())
                    .workId(kycUser.getId())
                    .email(kycUser.getUsername())
                    .build();

            return Optional.ofNullable(kycResponse);
        }


    }

    public String calculateAge(String dobString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate dob = LocalDate.parse(dobString, formatter);

        LocalDate currentDate = LocalDate.now();

        Period period = Period.between(dob, currentDate);

        int age = period.getYears();

        return String.valueOf(age);
    }

    public ResponseEntity<?> updateKYC(int id) {



        return null;
    }

}
