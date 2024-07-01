package com.appbackend.example.AppBackend.common;

import com.appbackend.example.AppBackend.entities.*;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.LoanReminder;
import com.appbackend.example.AppBackend.repositories.*;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.LoanReminderService;
import com.appbackend.example.AppBackend.services.OtpService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.appbackend.example.AppBackend.services.impl.PaymentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class Schedulers {

    Logger logger = LoggerFactory.getLogger(Schedulers.class);
    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private DisbursementInterestCountRepository disbursementInterestCountRepository;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CollectionAmountCalculationRepository  collectionAmountCalculationRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private MonthlyCollectionInfoRepository monthlyCollectionInfoRepository;

    @Autowired
    private DaysInArraysHistoryRepository daysInArraysHistoryRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private LoanReminderService loanReminderService;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(fixedRate = 150000)
    public  void updateDisbursementStatusAndUtilization(){
        System.out.println("Run Schedule:::");
        List<String> statuses = Arrays.asList(DisbursementsStatus.PENDING.name(), DisbursementsStatus.INITIALIZE.name());

        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository.findByPaymentStatusIn(statuses);
        System.out.println("Size Of Pending Status :: " + disbursementsHistoryList.size());
        disbursementsHistoryList.forEach(paymentService::checkDisbursementStatusAndUpdate);


        List<CollectionHistory> collectionHistoryList = collectionHistoryRepository.findByStatusIn(statuses);
        logger.info("Size Of Pending Status for collection :: " + collectionHistoryList.size());
        collectionHistoryList.forEach(collectionService::checkCollectionStatusAndUpdate);
    }


//    @Scheduled(cron = "0 0 0 * * *")
@Scheduled(fixedRate = 150000)

public void dailyJobScheduler(){
        // get all running loan
        System.out.println("JOB RUN");
        List<DisbursementsHistory> runningDisbursementsHistoryList = disbursementsRepository.findByPaymentStatusAndCollectionCompleted(DisbursementsStatus.SUCCEEDED.name() , false);
        Optional<InterestCountMaster> interestCountMasterOptional = interestRepository.findFirstByOrderById();
        runningDisbursementsHistoryList.forEach(disbursementsHistory -> {
            Integer userid = disbursementsHistory.getId();
            DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId());
            MonthlyCollectionInfo monthlyCollectionInfo = monthlyCollectionInfoRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId());

            if(disbursementInterestCount != null) {
                long lastInterestCountDay = DAYS.between(disbursementInterestCount.getInterestCalculationDate(), LocalDate.now());
                logger.info("InterestCount Days :: " + lastInterestCountDay);
                if (lastInterestCountDay > 8) {
                    logger.info("Start Calculation New Interest");
                    weeklyInterestCount(interestCountMasterOptional, disbursementsHistory, disbursementInterestCount, monthlyCollectionInfo);
                    logger.info("End Interest Count");
                }

                logger.info("Check Monthly End Record");
                long checkMonthEndRecord = DAYS.between(monthlyCollectionInfo.getMonthEndDate(), LocalDate.now());
                logger.info("Month End  : " + checkMonthEndRecord);
                if (checkMonthEndRecord >= 1) {
                    logger.info("Start Check Month end records");
                    if (monthlyCollectionInfo.getIsRescheduled() != null && monthlyCollectionInfo.getIsRescheduled()) {
                        long checkRescheduledDays = DAYS.between(monthlyCollectionInfo.getRescheduleDate(), LocalDate.now());
                        logger.info("Reschedule End Total Days :: " + checkRescheduledDays);
                        if (checkRescheduledDays >= 1) {
                            //Add Record in days in area table
                            AddOrUpdatedayInArrayRecord(disbursementsHistory, monthlyCollectionInfo);
                        }
                    } else {
                        if (monthlyCollectionInfo.isPayMinimumAmount()) {
                            MonthlyCollectionInfo monthlyCollectionInfoNew = PaymentServiceImpl.buildAndSaveMonthlyCollectionInfo(disbursementsHistory, monthlyCollectionInfo, interestCountMasterOptional.get());
                            CollectionAmountCalculation collectionAmountCalculationOld = collectionAmountCalculationRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId()).get();
                            monthlyCollectionInfoNew.setMinimumAmount(collectionAmountCalculationOld.getRemainingPayment());
                            monthlyCollectionInfoRepository.save(monthlyCollectionInfoNew);
                        } else {
                            //Add Record in days in area table
                            AddOrUpdatedayInArrayRecord(disbursementsHistory, monthlyCollectionInfo);

                        }
                    }
                }

                // Send Loan Reminder to user
                sendLoanReminder(disbursementsHistory, monthlyCollectionInfo);
            }
        });
    }

    private void sendLoanReminder(DisbursementsHistory disbursementsHistory, MonthlyCollectionInfo monthlyCollectionInfo) {
        long daysLoan = DAYS.between(disbursementsHistory.getCreatedOn() , LocalDateTime.now());
        logger.info("Loan:: " + daysLoan);
        String loanReminderMsg = loanReminderService.sentLoanReminderAfterLoanProcess(daysLoan);
        logger.info("Loan :: " + loanReminderMsg);
        User user = userRepository.findByid(disbursementsHistory.getUserId()).get();
        if(loanReminderMsg != null) {
            try {
                otpService.sendSms(user.getPhoneNumber(), null, loanReminderMsg);
                otpService.sendEmail(user.getEmail() , "Disbursement Reminder " , loanReminderMsg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        long daysDue = DAYS.between(monthlyCollectionInfo.getMonthEndDate() , LocalDate.now());
        logger.info("Loan After  :: " + daysDue);
        String loanReminderDue = loanReminderService.getReminderBeforeAndAfterDue(daysDue ,  monthlyCollectionInfo.getTotalPayAmountInMonth() == 0);
        logger.info("Loan sd:: " + loanReminderDue);
        if(loanReminderDue != null) {
            try {
                otpService.sendSms(user.getPhoneNumber(), null, loanReminderDue);
                otpService.sendEmail(user.getEmail() , "Disbursement Reminder " , loanReminderDue);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

//        if(monthlyCollectionInfo.getIsRescheduled()){
//            ReschedulePaymentRecord reschedulePaymentRecord = re
//            long daysReschedule = DAYS.between(monthlyCollectionInfo.getRescheduleDate() , LocalDate.now());
//            logger.info("Loan After  :: " + daysReschedule);
//            String loanReminderForReschedule = loanReminderService.getReminderForReschedule(daysDue);
//            logger.info("Loan sd:: " + loanReminderForReschedule);
//
//        }
    }

    private void AddOrUpdatedayInArrayRecord(DisbursementsHistory disbursementsHistory, MonthlyCollectionInfo monthlyCollectionInfo) {
        DaysInArraysHistory daysInArraysHistory = daysInArraysHistoryRepository.findFirstByDisbursementsHistoryIdAndMonthlyCollectionInfoId(disbursementsHistory.getId() , monthlyCollectionInfo.getId());

        //Add or update Record in days in area table
        if(Objects.isNull(daysInArraysHistory)) {
            daysInArraysHistory = new DaysInArraysHistory();
            daysInArraysHistory.setDisbursementsHistory(disbursementsHistory);
            daysInArraysHistory.setUserId(disbursementsHistory.getUserId());
            daysInArraysHistory.setMonthlyCollectionInfo(monthlyCollectionInfo);
            daysInArraysHistory.setEnterArraysDate(LocalDate.now());
            daysInArraysHistory.setTotalDays((int) DAYS.between(monthlyCollectionInfo.getMonthEndDate() , LocalDate.now() ));
        }else {
            daysInArraysHistory.setTotalDays(daysInArraysHistory.getTotalDays() + 1);
        }
        daysInArraysHistoryRepository.save(daysInArraysHistory);
    }

    private void weeklyInterestCount(Optional<InterestCountMaster> interestCountMasterOptional, DisbursementsHistory disbursementsHistory, DisbursementInterestCount disbursementInterestCount, MonthlyCollectionInfo monthlyCollectionInfo) {
        CollectionAmountCalculation collectionAmountCalculationOld = collectionAmountCalculationRepository.findFirstByDisbursementsHistoryIdOrderByIdDesc(disbursementsHistory.getId()).get();
        DisbursementInterestCount interestCount = disbursementInterestCountRepository.save(PaymentServiceImpl.buildDisbursementInterestCount(interestCountMasterOptional.get() , disbursementsHistory, collectionAmountCalculationOld , disbursementInterestCount));

        CollectionAmountCalculation collectionAmountCalculation = new CollectionAmountCalculation();
        collectionAmountCalculation.setTotalPayAmount(collectionAmountCalculationOld.getTotalPayAmount());
        collectionAmountCalculation.setRemainingPayment( Double.valueOf(interestCount.getEndingBalance()));
        collectionAmountCalculation.setUserId(disbursementsHistory.getUserId());
        collectionAmountCalculation.setLastTransactionDate(LocalDateTime.now());
        collectionAmountCalculation.setDisbursementsHistory(disbursementsHistory);
        collectionAmountCalculation.setDescription("Calculate Interest on remaining amount of :: " + interestCount.getBeginningBalance());

        collectionAmountCalculationRepository.save(collectionAmountCalculation);

        if( monthlyCollectionInfo.isLastMonth()){
            monthlyCollectionInfo.setMinimumAmount(collectionAmountCalculation.getRemainingPayment());
            monthlyCollectionInfoRepository.save(monthlyCollectionInfo);
        }

    }

}
