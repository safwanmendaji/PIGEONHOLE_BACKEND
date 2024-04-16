package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {


    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getAllUsers(){
        return  userRepository.findAll();
    }

    public Optional<User>  getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User>  getUserByPhone(String phone){
        return userRepository.findByPhoneNumber(phone);
    }

    public Optional<User> getUserById(Integer id){
        return  userRepository.findById(id);
    }



}
