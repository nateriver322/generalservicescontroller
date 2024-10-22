package com.generalservicesportal.joborder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.generalservicesportal.joborder.model.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
	User findByEmail(String email);
	User findByUsername(String username);
	 List<User> findByUsernameContainingIgnoreCase(String query);
	 boolean existsByEmail(String email);
	 List<User> findByRole(String role);
	 List<User> findByRoleIn(List<String> roles);
	 User findByResetPasswordToken(String token);
}
