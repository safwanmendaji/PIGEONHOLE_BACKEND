package com.appbackend.example.AppBackend.services.AdminServices;

import com.appbackend.example.AppBackend.entities.CreditScore;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.repositories.CreditScoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class CreditScoreService {

    @Autowired
    CreditScoreRepository creditScoreRepository;

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


        String blacklistedObj = objectMaker(blacklistedScore, blacklistedWeight, blacklistedExposure);
        String departmentObj = objectMaker(departmentScore, departmentWeight, departmentExposure);
        String salaryScaleObj = objectMaker(salaryScaleScore, salaryScaleWeight, salaryScaleExposure);
        String priorityClientObj = objectMaker(priorityClientScore, priorityClientWeight, priorityClientExposure);
        String securityObj = objectMaker(securityScore, securityWeight, securityClientExposure);


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


//        CreditScore creditScore = CreditScore.builder()
//                .user(user)
//                .id(user.getId())
//                .age(existingcreditScore.getAge())
//                .blacklisted(String.valueOf(blacklistedObj))
//                .workPlaceDeartment(String.valueOf(departmentObj))
//                .salaryScale(String.valueOf(salaryScaleObj))
//                .priorityClient(String.valueOf(priorityClientObj))
//                .security(String.valueOf(securityObj))
//                .build();

        System.out.println(blacklistedObj);
        System.out.println(departmentObj);
        System.out.println(salaryScaleObj);
        System.out.println(priorityClientObj);
        System.out.println(securityObj);


        creditScoreRepository.save(existingcreditScore);


    }

    public String objectMaker(int score, int weight, float exposure) {

        Map<String, Object> objMap = new HashMap<>();

        objMap.put("weight", weight);
        objMap.put("score", score);
        objMap.put("exposure", exposure);

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            return objectMapper.writeValueAsString(objMap);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public int calculateAgeCreditScore(int age) {

        if (age >= 19 && age <= 40) {
            return 2;
        }
        if (age >= 41 && age <= 57) {
            return 3;
        }
        if (age >= 58 && age <= 60) {
            return 4;
        }
        return 0;
    }

    public int calculateGenderCreditScore(String gender) {
        if (gender.equals("MALE")) {
            return 3;
        }
        if (gender.equals("FEMALE")) {
            return 2;
        }

        return 0;

    }

    public int calculateNextOfKin(String kin) {

        if (kin.equals("SPOUSE")) {
            return 2;
        }
        if (kin.equals("WORK MATE")) {
            return 3;
        }
        if (kin.equals("RELATIVE")) {
            return 3;
        }
        if (kin.equals("FRIEND")) {
            return 4;
        }

        return 0;


    }

}




