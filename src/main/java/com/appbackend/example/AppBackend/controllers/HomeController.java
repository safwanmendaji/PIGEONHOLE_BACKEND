package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.services.UserService;
import com.appbackend.example.AppBackend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private UserService userService;

    //    http://localhost:8081/home/user
    @GetMapping("/users")
    public List<User> getUsers() {

        System.out.println("getting users");

        return userService.getAllUsers();
    }

    @GetMapping("/current-user")
    public String getLoggedInUser(Principal principal) {
        return principal.getName();
    }
    
  
    
}
