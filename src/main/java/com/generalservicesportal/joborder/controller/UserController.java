package com.generalservicesportal.joborder.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.generalservicesportal.joborder.model.MicrosoftLoginRequest;
import com.generalservicesportal.joborder.model.PersonnelLoginRequest;
import com.generalservicesportal.joborder.model.User;
import com.generalservicesportal.joborder.service.MicrosoftAuthService;
import com.generalservicesportal.joborder.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private MicrosoftAuthService microsoftAuthService;
    
    
    
    
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsersByRole(@RequestParam String role) {
        try {
            List<User> users = userService.findByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable int id, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        if (role == null || !role.equals("Personnel")) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        User updatedUser = userService.updateUserRole(id, role);
        if (updatedUser != null) {
            return ResponseEntity.ok("User role updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/subrole")
    public ResponseEntity<?> assignSubrole(@PathVariable int id, @RequestParam String subrole) {
        User updatedUser = userService.assignSubrole(id, subrole);
        if (updatedUser != null) {
            return ResponseEntity.ok("Subrole assigned successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    @GetMapping("/{username}/details")
    public ResponseEntity<?> getUserDetailsByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        if (user != null) {
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("username", user.getUsername());
            userDetails.put("role", user.getRole());
            userDetails.put("subrole", user.getSubrole());
            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    
    @PostMapping("/microsoft-login")
    public ResponseEntity<?> microsoftLogin(@RequestBody MicrosoftLoginRequest request) {
        try {
            String email = microsoftAuthService.validateTokenAndGetEmail(request.getToken());
            User user = userService.findOrCreateMicrosoftUser(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid Microsoft token: " + e.getMessage());
        }
    }

@PostMapping("/personnel-login")
    public ResponseEntity<?> personnelLogin(@RequestBody PersonnelLoginRequest request) {
        try {
            User user = userService.findUserByPersonnelId(request.getPersonnelId());
            
            if (user != null && "Personnel".equals(user.getRole())) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("role", user.getRole());
                response.put("subrole", user.getSubrole());
                response.put("email", user.getEmail());
                response.put("contactNumber", user.getContactNumber());
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid personnel ID");
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during login");
        }
    }


    
    

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody User user) {
        User existingUser = userService.findUserByEmail(user.getEmail());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // The password will be encoded in the saveUser method
        User savedUser = userService.saveUser(user);
        if (savedUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering user");
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User existingUser = userService.findUserByEmail(user.getEmail());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // The password will be encoded in the saveUser method
        User savedUser = userService.saveUser(user);
        if (savedUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering user");
        }
    }
    

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            User user = userService.findUserByEmail(loginUser.getEmail());
            if (user != null) {
                if (user.isMicrosoftAuth()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please use Microsoft login for this account");
                }
                if (userService.matchesPassword(loginUser.getPassword(), user.getPassword())) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Login successful");
                    response.put("username", user.getUsername());
                    response.put("role", user.getRole());
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect email or password");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    
    @GetMapping("/accounts")
    public List<User> getAllAccounts() {
        return userService.findAllUsers();
    }
    
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String query) {
        return userService.searchUsersByUsername(query);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User updatedUser) {
        User existingUser = userService.findUserById(id);
        if (existingUser != null) {
            // Update only the role if provided
            if (updatedUser.getRole() != null) {
                existingUser.setRole(updatedUser.getRole());
            }

            // Only update the password if it's provided and different
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                if (!updatedUser.getPassword().startsWith("$2a$")) {
                    existingUser.setPassword(userService.encodePassword(updatedUser.getPassword()));
                }
            }

            // Save the user with updated role (and optionally password), but keep other fields unchanged
            userService.saveUser(existingUser);
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    
    @GetMapping("/personnel")
    public ResponseEntity<List<User>> getPersonnel() {
        try {
            List<User> personnel = userService.findPersonnel();
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        User existingUser = userService.findUserById(id);
        if (existingUser != null) {
            userService.deleteUserById(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    
    @GetMapping("/checkEmail")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }
    
    
    
    
    

}
