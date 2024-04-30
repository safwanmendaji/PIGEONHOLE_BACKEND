package com.appbackend.example.AppBackend.services.AdminServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.appbackend.example.AppBackend.entities.KycCalculationDetails;
import com.appbackend.example.AppBackend.entities.LoanEligibility;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.UserKYCDto;
import com.appbackend.example.AppBackend.repositories.LoanEligibilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.appbackend.example.AppBackend.entities.CreditScore;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.CreditScoreDTO;
import com.appbackend.example.AppBackend.models.CreditScoreDtoDemo;
import com.appbackend.example.AppBackend.repositories.CreditScoreRepository;
import com.appbackend.example.AppBackend.repositories.KycCalculationDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@PreAuthorize("hasAuthority('ADMIN')")
public class CreditScoreService {

    @Autowired
    CreditScoreRepository creditScoreRepository;
    
    
    @Autowired
    KycCalculationDetailsRepository kycCalculationDetailsRepository;

    @Autowired
    LoanEligibilityRepository loanEligibilityRepository;

    @Value("${credit.offer.per.level}")
    private String creditOfferPerLevel;

    public void calculateCreditScore(Map<String, Map<String, Object>> objectMap) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        System.out.println(user.getFirstName());

        CreditScore existingcreditScore = creditScoreRepository.findCreditScoreById(user.getId());
        int storedCreditScore = existingcreditScore.getTotalCreditScore();
        if (storedCreditScore != 0) {

        }


        int VERY_LOW_RISK = 1;
        int LOW_RISK = 2;
        int MODERATE_RISK = 3;
        int HIGH_RISK = 4;
        int VERY_HIGH_RISK = 5;


        Map<String, Integer> creditFactorsMap = new HashMap<>();
//Blacklisted
        creditFactorsMap.put("blacklisted", VERY_HIGH_RISK);
        creditFactorsMap.put("Not blacklisted", VERY_LOW_RISK);
//        Departmemts
        creditFactorsMap.put("Department of Clerks", LOW_RISK);
        creditFactorsMap.put("Parliamentary Budget Office", LOW_RISK);
        creditFactorsMap.put("Department of Research Services", LOW_RISK);
        creditFactorsMap.put("Department of Official Report", MODERATE_RISK);
        creditFactorsMap.put("Office of  Speaker", MODERATE_RISK);
        creditFactorsMap.put("Office of Deputy Speaker", MODERATE_RISK);
        creditFactorsMap.put("Office of the Leader of Govt Business", MODERATE_RISK);
        creditFactorsMap.put("Office of the Leader of Opposition", MODERATE_RISK);
        creditFactorsMap.put("Department of Legislative and procedural services", MODERATE_RISK);
        creditFactorsMap.put("Department of Litigation and compliance", MODERATE_RISK);
        creditFactorsMap.put("Commission Secretariat", MODERATE_RISK);
        creditFactorsMap.put("Internal Audit", MODERATE_RISK);
        creditFactorsMap.put("Department of ICT", MODERATE_RISK);
        creditFactorsMap.put("Department of Library services", MODERATE_RISK);
        creditFactorsMap.put("Human Resource Department", MODERATE_RISK);
        creditFactorsMap.put("Department of Finance", MODERATE_RISK);
        creditFactorsMap.put("Department of Communication and Public Affairs", MODERATE_RISK);
        creditFactorsMap.put("Department of Corporate Planning and Strategy", MODERATE_RISK);
        creditFactorsMap.put("Department of Sergent- At -Arms", HIGH_RISK);
        creditFactorsMap.put("Department of Administration and Transport Logistics", HIGH_RISK);
//        SALARY SCALE
        creditFactorsMap.put("PC1", LOW_RISK);
        creditFactorsMap.put("PC2", LOW_RISK);
        creditFactorsMap.put("PC3", LOW_RISK);
        creditFactorsMap.put("PC4", MODERATE_RISK);
        creditFactorsMap.put("PC5", MODERATE_RISK);
        creditFactorsMap.put("PC6", HIGH_RISK);
        creditFactorsMap.put("PC7", HIGH_RISK);
        creditFactorsMap.put("PC8", HIGH_RISK);
//        PRIORITY CLIENT
        creditFactorsMap.put("1", VERY_LOW_RISK);
        creditFactorsMap.put("2", LOW_RISK);
        creditFactorsMap.put("3", MODERATE_RISK);
        creditFactorsMap.put("4", HIGH_RISK);
        creditFactorsMap.put("5", VERY_HIGH_RISK);
//        SECURITY
        creditFactorsMap.put("TITLE", VERY_LOW_RISK);
        creditFactorsMap.put("LOG BOOK", MODERATE_RISK);
        creditFactorsMap.put("KYIBANJA ", MODERATE_RISK);
        creditFactorsMap.put("NO SECURITY ", VERY_HIGH_RISK);

//        1
        String blacklisted = (String) objectMap.get("BLACK_LISTED").get("type");
        Integer blacklistedWeight = (int) objectMap.get("BLACK_LISTED").get("weight");
        Integer blacklistedScore = creditFactorsMap.get(blacklisted);
        float blacklistedExposure = blacklistedWeight > 0 ? (1 / (float) blacklistedScore) * blacklistedWeight : ((float) (blacklistedScore - 1) / 4) * blacklistedWeight;

//2
        String department = (String) objectMap.get("DEPARTMENTS").get("type");
        Integer departmentWeight = (int) objectMap.get("DEPARTMENTS").get("weight");
        Integer departmentScore = creditFactorsMap.get(department);
        float departmentExposure = departmentWeight > 0 ? (1 / (float) departmentScore) * departmentWeight : ((float) (departmentScore - 1) / 4) * departmentWeight;

//3
        String salaryScale = (String) objectMap.get("SALARY_SCALE").get("type");
        Integer salaryScaleWeight = (int) objectMap.get("SALARY_SCALE").get("weight");
        Integer salaryScaleScore = creditFactorsMap.get(salaryScale);
        float salaryScaleExposure = salaryScaleWeight > 0 ? (1 / (float) salaryScaleScore) * salaryScaleWeight : ((float) (salaryScaleScore - 1) / 4) * salaryScaleWeight;

//4
        String priorityClient = (String) objectMap.get("PRIORITY_CLIENT").get("type");
        Integer priorityClientWeight = (int) objectMap.get("PRIORITY_CLIENT").get("weight");
        Integer priorityClientScore = creditFactorsMap.get(priorityClient);
        float priorityClientExposure = priorityClientWeight > 0 ? (1 / (float) priorityClientScore) * priorityClientWeight : ((float) (priorityClientScore - 1) / 4) * priorityClientWeight;

//        5
        String security = (String) objectMap.get("SECURITY").get("type");
        Integer securityWeight = (int) objectMap.get("SECURITY").get("weight");
        Integer securityScore = creditFactorsMap.get(security);
        float securityClientExposure = securityWeight > 0 ? (1 / (float) securityScore) * securityWeight : ((float) (securityScore - 1) / 4) * securityWeight;

        Integer offerPerLevel = (Integer) objectMap.get("OFFER_PER_LEVEL").get("value");


        ObjectMapper objectMapper = new ObjectMapper();

//        String storedAgeCreditScoreStrr=existingcreditScore.getAge();
        Map<String, Object> storedAgeCreditObj = null;
        Map<String, Object> storedGenderCreditObj = null;
        Map<String, Object> storedKinCreditObj = null;

        try {
            storedAgeCreditObj = objectMapper.readValue(existingcreditScore.getAge(), Map.class);
            storedGenderCreditObj = objectMapper.readValue(existingcreditScore.getGender(), Map.class);
            storedKinCreditObj = objectMapper.readValue(existingcreditScore.getNextOfKinType(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        int storedAgeCreditScore = (int) storedAgeCreditObj.get("score");
        int storedGenderCreditScore = (int) storedGenderCreditObj.get("score");
        int storedKinCreditScore = (int) storedKinCreditObj.get("score");


        System.out.println(storedAgeCreditObj);
        System.out.println("AGE OBJ IS HERE " + storedAgeCreditScore + "--------------");

        int totalCreditScore = storedAgeCreditScore + storedGenderCreditScore + storedKinCreditScore + blacklistedScore + departmentScore + salaryScaleScore + priorityClientScore + securityScore;
        System.out.println(totalCreditScore);

        int totalWeight = blacklistedWeight + departmentWeight + salaryScaleWeight + priorityClientWeight + securityWeight;
        System.out.println(totalWeight);

        float totalExposure = blacklistedExposure + departmentExposure + salaryScaleExposure + priorityClientExposure + securityClientExposure;

        int totalCreditScoreValue = totalCreditScore * 5;

        float averageCreditScoreValue = totalCreditScoreValue / 5;

        float availableOffer = (float) (Math.ceil((totalExposure / 100) * offerPerLevel / 1000.0) * 1000);


        System.out.println(" total weight " + totalWeight);
        System.out.println(" total score " + totalCreditScore);
        System.out.println("  total exposure " + totalExposure);
        System.out.println(" average credit score " + averageCreditScoreValue);
        System.out.println("  credit score value " + totalCreditScoreValue);
        System.out.println(" available offer " + availableOffer);


        String blacklistedObj = makeCreditScoreOjb(blacklistedScore, blacklistedWeight, blacklistedExposure ,0);
        String departmentObj = makeCreditScoreOjb(departmentScore, departmentWeight, departmentExposure ,0);
        String salaryScaleObj = makeCreditScoreOjb(salaryScaleScore, salaryScaleWeight, salaryScaleExposure ,0);
        String priorityClientObj = makeCreditScoreOjb(priorityClientScore, priorityClientWeight, priorityClientExposure,0);
        String securityObj = makeCreditScoreOjb(securityScore, securityWeight, securityClientExposure,0);


        existingcreditScore.setAverageCreditScoreValue(averageCreditScoreValue);
        existingcreditScore.setTotalCreditScore(totalCreditScoreValue);
        existingcreditScore.setBlacklisted(String.valueOf(blacklistedObj));
        existingcreditScore.setSalaryScale(String.valueOf(salaryScaleObj));
        existingcreditScore.setPriorityClient(String.valueOf(priorityClientObj));
        existingcreditScore.setSecurity(String.valueOf(securityObj));
        existingcreditScore.setTotalExposure(totalExposure);
        existingcreditScore.setWorkPlaceDeartment(String.valueOf(departmentObj));
        existingcreditScore.setAvailableOffer(availableOffer);
        existingcreditScore.setTotalWeight(totalWeight);



        System.out.println(blacklistedObj);
        System.out.println(departmentObj);
        System.out.println(salaryScaleObj);
        System.out.println(priorityClientObj);
        System.out.println(securityObj);


        creditScoreRepository.save(existingcreditScore);


    }

    public static String makeCreditScoreOjb(Object score, int weight, Object exposure , int kycCalculationId) {

        Map<String, Object> objMap = new HashMap<>();

        objMap.put("weight", weight);
        objMap.put("score", score);
        objMap.put("exposure", exposure);
        objMap.put("kycCalculationId", kycCalculationId);

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            return objectMapper.writeValueAsString(objMap);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public int ageCreditScore(int age) {
        if (age >= 19 && age <= 40) {
            return  2;
        }
        if (age >= 41 && age <= 57) {
            return   3;
        }
        if (age >= 58 && age <= 60) {
            return  4;
        }
        return 0;
    }

    public int genderCreditScore(String gender) {
        if (gender.equalsIgnoreCase("MALE")) {
            return 3;
        }
        if (gender.equalsIgnoreCase("FEMALE")) {
            return 2;
        }

        return 0;

    }

    public int nextOfKin(String kin) {

        if (kin.equalsIgnoreCase("SPOUSE")) {
            return 2;
        }
        if (kin.equalsIgnoreCase("WORK MATE")) {
            return 3;
        }
        if (kin.equalsIgnoreCase("RELATIVE")) {
            return 3;
        }
        if (kin.equalsIgnoreCase("FRIEND")) {
            return 4;
        }

        return 0;


    }


    public CreditScoreDTO findByUserId(Integer userId) {
        CreditScore creditScore = creditScoreRepository.findCreditScoreById(userId);
        long offerPerLevel = Long.parseLong(creditOfferPerLevel);
        double exposure = creditScore.getTotalExposure() != null ? creditScore.getTotalExposure() : 0.0;
        double availableOffer = ((double) exposure / 100) * offerPerLevel;

        if (creditScore != null) {
            CreditScoreDTO creditScoreDTO = new CreditScoreDTO();
            creditScoreDTO.setTotalCreditScore(creditScore.getTotalCreditScore());
            creditScoreDTO.setAvailableOffer(Math.round(availableOffer));
            creditScoreDTO.setTotalExposure(creditScore.getTotalExposure());
            creditScoreDTO.setOfferPerLevel(offerPerLevel);
            return creditScoreDTO;
        } else {
            return null;
        }


    }

    public double calculateCommonCreditScore(int score , int weight) {

        float sc = (float)1/score;
        return sc * weight;

    }


    public double calculateNegativeCreditScore(int score,int weight) {
        float sc = ((float)score-1)/4;
        return sc * weight;
    }


    public CreditScoreDTO getCreditScore(UserKYCDto creditScoreDtoDemo) {
    try {
        Optional<CreditScore> optionalCreditScore = creditScoreRepository.findByUserId(creditScoreDtoDemo.getUserId());

        if (optionalCreditScore.isPresent()) {
            CreditScore creditScore = optionalCreditScore.get();

            List<KycCalculationDetails> allDetails = kycCalculationDetailsRepository.findAll();
            Map<String, List<KycCalculationDetails>> groupedByWorkplace = allDetails.stream()
                    .collect(Collectors.groupingBy(KycCalculationDetails::getWorkPlaceName));

            int totalCreditScore =0;

            Double blacklistedCreditScore = 0.0;
            Double workPlaceDepartmentCreditScore = 0.0;
            Double occupationCreditScore = 0.0;
            Double amountInArrears = 0.0;
            Double daysInArrears = 0.0;
            Double rescheduleHistory = 0.0;
            Double priorityClient =0.0;
            Double security= 0.0;
            Double loanHistoryLoansWithArrears = 0.0;
            Double loanHistoryLoansWithoutArrears = 0.0;


            String blacklistedCreditScoreString = "";
            String workPlaceDepartmentCreditScoreString="";
            String occupationCreditScoreString = "" ;
            String amountInArrearsString = "" ;
            String daysInArrearsString = "" ;
            String rescheduleHistoryString = "";
            String priorityClientString = "";
            String securityString = "";
            String loanHistoryLoansWithArrearsString="";
            String loanHistoryLoansWithoutArrearsString ="";

            if(creditScoreDtoDemo.getBlacklisted() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("BLACKLISTED");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getBlacklisted());
                blacklistedCreditScore = calculateNegativeCreditScore(score, -10);
                blacklistedCreditScoreString = makeCreditScoreOjb(score, -10, blacklistedCreditScore , creditScoreDtoDemo.getBlacklisted());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getWorkPlaceDepartment() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("DEPARTMENTS");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getWorkPlaceDepartment());
                workPlaceDepartmentCreditScore = calculateCommonCreditScore(score, 5);
                workPlaceDepartmentCreditScoreString = makeCreditScoreOjb(score, 5, workPlaceDepartmentCreditScore , creditScoreDtoDemo.getWorkPlaceDepartment());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getOccupation() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("OCCUPATION");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getOccupation());
                occupationCreditScore = calculateCommonCreditScore(score, 1);
                occupationCreditScoreString = makeCreditScoreOjb(score, 1, occupationCreditScore, creditScoreDtoDemo.getOccupation());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getAmountInArrears() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("ARREARS AMOUNT (DEFAULT)");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getAmountInArrears());
                amountInArrears = calculateNegativeCreditScore(score, -10);
                amountInArrearsString = makeCreditScoreOjb(score, -10, amountInArrears , creditScoreDtoDemo.getAmountInArrears());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getDaysInArreas() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("DAYS IN ARREARS (PAYMENT HISTORY)");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getDaysInArreas());
                daysInArrears = calculateCommonCreditScore(score, 10);
                daysInArrearsString = makeCreditScoreOjb(score, 10, daysInArrears , creditScoreDtoDemo.getDaysInArreas());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getRescheduleHistory() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("RESCHEDULE");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getRescheduleHistory());
                rescheduleHistory = calculateCommonCreditScore(score, 1);
                rescheduleHistoryString = makeCreditScoreOjb(score, 1, rescheduleHistory , creditScoreDtoDemo.getRescheduleHistory());
                totalCreditScore += score;
            }

            if(creditScoreDtoDemo.getPriorityClient() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("PRIORITY CLIENT");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getPriorityClient());

                priorityClient = calculateCommonCreditScore(score, 15);
                priorityClientString = makeCreditScoreOjb(score, 15, priorityClient , creditScoreDtoDemo.getPriorityClient());
                totalCreditScore += score;

            }

            if(creditScoreDtoDemo.getSecurity() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("SECURITY");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getSecurity());

                security = calculateCommonCreditScore(score, 1);
                securityString = makeCreditScoreOjb(score, 1, security , creditScoreDtoDemo.getSecurity());
                totalCreditScore += score;

            }

            if(creditScoreDtoDemo.getLoanHistoryLoansWithArrears() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("LOAN HISTORY (COMPLETED LOANS WITH ARREARS) (NEGATIVE)");
                int score  = getScore(kycCalculationDetails, creditScoreDtoDemo.getLoanHistoryLoansWithArrears());
                loanHistoryLoansWithArrears = calculateNegativeCreditScore(score, -60);
                loanHistoryLoansWithArrearsString = makeCreditScoreOjb(score, -60, loanHistoryLoansWithArrears , creditScoreDtoDemo.getLoanHistoryLoansWithArrears());
                totalCreditScore += score;

            }


            if(creditScoreDtoDemo.getLoanHistoryLoansWithOutArrears() != null) {
                List<KycCalculationDetails> kycCalculationDetails = groupedByWorkplace.get("LOAN HISTORY (COMPLETED LOANS WITH OUT ARREARS) (POSITIVE)");
                int score = getScore(kycCalculationDetails, creditScoreDtoDemo.getLoanHistoryLoansWithOutArrears());
                loanHistoryLoansWithoutArrears = calculateCommonCreditScore(score, 60);
                loanHistoryLoansWithoutArrearsString = makeCreditScoreOjb(score, 60, loanHistoryLoansWithoutArrears , creditScoreDtoDemo.getLoanHistoryLoansWithOutArrears());
                totalCreditScore += score;

            }

            double ageScore = findOldExposureValue(creditScore.getAge());
            double genScore = findOldExposureValue(creditScore.getGender());
            double kinScore = findOldExposureValue(creditScore.getNextOfKinType());

            totalCreditScore = totalCreditScore + findOldScoreValue(creditScore.getAge()) + findOldScoreValue(creditScore.getGender()) + findOldScoreValue(creditScore.getNextOfKinType());

            float totalExposure = (float) (blacklistedCreditScore + workPlaceDepartmentCreditScore +
                    occupationCreditScore + amountInArrears + daysInArrears +
                    rescheduleHistory + priorityClient + security +
                    loanHistoryLoansWithArrears + loanHistoryLoansWithoutArrears + genScore + ageScore + kinScore);


            creditScore.setTotalCreditScore(totalCreditScore);
            creditScore.setTotalExposure(totalExposure);

            creditScore.setBlacklisted(blacklistedCreditScoreString);
            creditScore.setSecurity(securityString);
            creditScore.setAmountInArrears(amountInArrearsString);
            creditScore.setLoanHistoryLoansWithArrears(loanHistoryLoansWithArrearsString);
            creditScore.setLoanHistoryLoansWithOutArrears(loanHistoryLoansWithoutArrearsString);
            creditScore.setOccupation(occupationCreditScoreString);
            creditScore.setWorkPlaceDepartment(workPlaceDepartmentCreditScoreString);
            creditScore.setDaysInArrears(daysInArrearsString);
            creditScore.setRescheduledHistory(rescheduleHistoryString);
            creditScore.setPriorityClient(priorityClientString);

            creditScoreRepository.save(creditScore);

            CreditScoreDTO creditScoreDTO = new CreditScoreDTO();
            return creditScoreDTO;
        } else {
            return null;
        }
    }catch (Exception e){
        e.printStackTrace();
         new Exception("SOME THING WENT WRONG IN UPDATE CREDIT SCORE.");

    }
    return null;
    }

    private static int getScore(List<KycCalculationDetails> kycCalculationDetails, int creditScoreDtoDemo) {
        int score = kycCalculationDetails.stream()
                .filter(calculationDetails -> calculationDetails.getId() == creditScoreDtoDemo)
                .mapToInt(KycCalculationDetails::getScore)
                .findFirst()
                .orElse(0);
        return score;
    }


    private double findOldExposureValue(String value){
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(value);

            // Retrieve the value associated with the "score" key
            double score = root.get("exposure").asDouble();
            return score;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    public static int findOldScoreValue(String value){
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(value);

            // Retrieve the value associated with the "score" key
            int score = root.get("score").asInt();
            return score;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }


    public static int findOldKycCalculationIdValue(String value){
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(value);
            int kycCalculationId = 0;
            if (root.has("kycCalculationId")) {
                // Retrieve the value associated with the "kycCalculationId" key
                kycCalculationId = root.get("kycCalculationId").asInt();
            }
            return kycCalculationId;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

   

    
}




