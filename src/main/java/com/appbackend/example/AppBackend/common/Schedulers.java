package com.appbackend.example.AppBackend.common;

import com.appbackend.example.AppBackend.entities.CollectionHistory;
import com.appbackend.example.AppBackend.entities.DisbursementInterestCount;
import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.repositories.CollectionHistoryRepository;
import com.appbackend.example.AppBackend.repositories.DisbursementInterestCountRepository;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.services.impl.CollectionServiceImpl;
import com.appbackend.example.AppBackend.services.impl.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class Schedulers {

    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private DisbursementInterestCountRepository disbursementInterestCountRepository;

    @Scheduled(fixedRate = 150000)
    public  void updateDisbursementStatusAndUtilization(){
        System.out.println("Run Schedule:::");
        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository.findByPaymentStatus(DisbursementsStatus.INITIALIZE.name());
        System.out.println("Size Of Pending Status :: " + disbursementsHistoryList.size());
        PaymentServiceImpl paymentServiceImpl = new PaymentServiceImpl();
        disbursementsHistoryList.forEach(paymentServiceImpl::checkDisbursementStatusAndUpdate);

        List<CollectionHistory> collectionHistoryList = collectionHistoryRepository.findByStatus(DisbursementsStatus.INITIALIZE.name());
        System.out.println("Size Of Pending Status for collection :: " + collectionHistoryList.size());
        CollectionServiceImpl collectionServiceImpl = new CollectionServiceImpl();
        collectionHistoryList.forEach(collectionServiceImpl::checkCollectionStatusAndUpdate);
    }


    @Scheduled(cron = "0 0 0 * * *")
    public void dailyJobScheduler(){
        // first need to check the end of month for all loan
//        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository
//        DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.findFistByDisbursementsHistoryIdOrderByIdDesc();
//        long interestCountDays = ChronoUnit.DAYS.between(disbursementInterestCount.getInterestCalculationDate(), LocalDate.now());


        //check the all user payment monthly

        //
    }

}
