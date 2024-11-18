package com.generalservicesportal.joborder.service;

import java.util.List;

import com.generalservicesportal.joborder.UserNotFoundException;
import com.generalservicesportal.joborder.model.User;

public interface UserService {
	public User saveUser(User user);
	public User findUserByEmail(String email);
	User findUserByUsername(String username);
	User findUserById(int id);
	List<User> findAllUsers();
	void deleteUserById(int id);
	List<User> searchUsersByUsername(String query);
	boolean existsByEmail(String email);
	List<User> findPersonnel();
	String encodePassword(String password);
	boolean matchesPassword(String rawPassword, String encodedPassword);
	User findOrCreateMicrosoftUser(String email);
	User assignSubrole(int userId, String subrole);
	
    User updateUserRole(int id, String role);
    List<User> findByRole(String role);
    List<User> findByRoles(List<String> roles);
    
    void updateResetPassword(String token,String email) throws UserNotFoundException;
	void updatePassword(User user, String newPassword);
	User get(String resetPasswordToken);


	User findUserByPersonnelId(String personnelId);
    boolean existsByPersonnelId(String personnelId);
}
