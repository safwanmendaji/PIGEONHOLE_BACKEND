package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.User;
import jakarta.persistence.Id;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



public interface UserRepository extends JpaRepository<User, Integer> {



    Optional<User> findByEmail(String Email);

    List<User> findAll();

//    Page<User> findAll(Pageable pageable);
    Optional<User> findByid(Integer id);


    Optional<User> findByPhoneNumber(String username);
}
