package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Integer userId;
	private String firstName;
	private String lastName;
	private String mobile;
	private String email;
	private int score;
	private String status;

	public UserDto(Integer userId, String firstName, String lastName, String mobile, String email, int score, String status) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mobile = mobile;
		this.email = email;
		this.score = score;
		this.status = status;
	}

	public UserDto(Integer userId, String firstName, String lastName, String mobile, String email, int score) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mobile = mobile;
		this.email = email;
		this.score = score;
	}



}
