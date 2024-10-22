package com.generalservicesportal.joborder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private int id;
	 private String username;
	 private String password;
	 private String contactNumber;
	 private String email;
	 private String role = "User";
	 private String subrole;
	 private boolean isMicrosoftAuth;
	 private String resetPasswordToken;
	 
	 
	public User() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	 public String getRole() {
	        return role;
	    }

	    public void setRole(String role) {
	        this.role = role;
	    }
	    public boolean isMicrosoftAuth() {
	        return isMicrosoftAuth;
	    }

	    public void setMicrosoftAuth(boolean microsoftAuth) {
	        isMicrosoftAuth = microsoftAuth;
	    }
	    
	    public String getSubrole() {
	        return subrole;
	    }

	    public void setSubrole(String subrole) {
	        this.subrole = subrole;
	    }

		public String getResetPasswordToken() {
			return resetPasswordToken;
		}

		public void setResetPasswordToken(String resetPasswordToken) {
			this.resetPasswordToken = resetPasswordToken;
		}

}
