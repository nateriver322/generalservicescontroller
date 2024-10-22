package com.generalservicesportal.joborder.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.generalservicesportal.joborder.UserNotFoundException;
import com.generalservicesportal.joborder.model.Ticket;
import com.generalservicesportal.joborder.model.User;
import com.generalservicesportal.joborder.repository.TicketRepository;
import com.generalservicesportal.joborder.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private BCryptPasswordEncoder passwordEncoder;
    
    public UserServiceImpl() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    @Override
    public void updateResetPassword(String token, String email)throws UserNotFoundException{
        User user = userRepository.findByEmail(email);

        if(user!=null){
            user.setResetPasswordToken(token);
            userRepository.save(user);

        }else{
            throw new UserNotFoundException("Could not find user with this email"+ email);
        }

    }
    
    public User get(String resetPasswordToken){
        return userRepository.findByResetPasswordToken(resetPasswordToken);
    }
    
    public void updatePassword(User user,String newPassword){
        BCryptPasswordEncoder passwordEncoder =new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);

        userRepository.save(user);
    }
    
    @Override
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(encodePassword(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public User findOrCreateMicrosoftUser(String email) {
        User user = findUserByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setRole("User");
            user.setMicrosoftAuth(true);
            // Set a random password for Microsoft users
            user.setPassword(UUID.randomUUID().toString());
            user = saveUser(user);
        }
        return user;
    }
    
    @Override
    public User assignSubrole(int userId, String subrole) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && "Personnel".equals(user.getRole())) {
            user.setSubrole(subrole);
            return userRepository.save(user);
        }
        return null;
    }
    
    
    
    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
    
  

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public User findUserById(int id) {  // Add this method
        return userRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public void deleteUserById(int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Delete the tickets associated with the user
            List<Ticket> tickets = ticketRepository.findByUsername(user.getUsername());
            if (tickets != null) {
                ticketRepository.deleteAll(tickets);
            }
            // Delete the user
            userRepository.deleteById(id);
        }
    }
    
    
    @Override
    public User updateUserRole(int id, String role) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(role);
            return userRepository.save(user);
        }
        return null;
    }
    
    @Override
    public List<User> findByRoles(List<String> roles) {
        return userRepository.findByRoleIn(roles);
    }
    
    
    @Override
    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    
    @Override
    public List<User> findPersonnel() {
        return userRepository.findByRole("Personnel");
    }
    
    @Override
    public List<User> searchUsersByUsername(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
