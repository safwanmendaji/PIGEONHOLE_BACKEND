package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private Integer userId;
	private String firstName;
	private String mobile;
	private String email;
	private int score;
	private boolean isApproved;
	

}
