package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.LoanReminderMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanReminderRepository extends JpaRepository<LoanReminderMessages , Integer> {
    LoanReminderMessages findByLoanReminder(String name);
}
