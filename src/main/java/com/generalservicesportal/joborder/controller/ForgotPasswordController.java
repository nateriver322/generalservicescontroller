    package com.generalservicesportal.joborder.controller;


    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;


import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
    import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.mail.MessagingException;
    import jakarta.mail.internet.MimeMessage;


import java.io.UnsupportedEncodingException;
    import java.util.Map;



    import java.util.HashMap;
    import java.security.SecureRandom;
    import java.util.Base64;

    import com.generalservicesportal.joborder.repository.UserRepository;
import com.generalservicesportal.joborder.service.UserService;

import com.generalservicesportal.joborder.model.User;
import org.springframework.web.bind.annotation.RequestMapping;


    @RestController
    @CrossOrigin(origins = "https://generalservices.vercel.app")
    @RequestMapping("/api")
    public class ForgotPasswordController {

 
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JavaMailSender mailSender;

@Autowired
private UserService userService;


        @PostMapping("/forgot_password")
        public ResponseEntity<?> processForgotPasswordForm(@RequestBody Map<String, String> payload) {
            String email = payload.get("email");
            String token = generateToken();

            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            user.setResetPasswordToken(token);
            userRepository.save(user);

            try {
                sendResetPasswordEmail(user.getEmail(), token);
            } catch (MessagingException | UnsupportedEncodingException e) {
                return ResponseEntity.internalServerError().body("Error sending email: " + e.getMessage());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link sent to your email");
            return ResponseEntity.ok(response);
        }

        private String generateToken() {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[33];
            random.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        private void sendResetPasswordEmail(String recipientEmail, String token)
                throws MessagingException, UnsupportedEncodingException {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("jobtrackcit@gmail.com", "Property Custodian Admin");
            helper.setTo(recipientEmail);

            String subject = "Here's the link to reset your password";

            String content = "<p>Hello,</p>"
                    + "<p>You have requested to reset your password.</p>"
                    + "<p>Click the link below to change your password:</p>"
                    + "<p><a href=\"" + "https://generalservices.vercel.app/reset_password?token=" + token + "\">Change my password</a></p>"
                    + "<br>"
                    + "<p>Ignore this email if you do remember your password, "
                    + "or you have not made the request.</p>";

            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        }

       

       @GetMapping("/reset_password")
public String showResetPasswordForm(@RequestParam(value = "token") String token, Model model) {
    User user = userRepository.findByResetPasswordToken(token);  // Adjust method name if different

    if (user == null) {
        model.addAttribute("title", "Reset your Password");
        model.addAttribute("message", "Invalid token");
        return "message";  // Redirect to a template showing the error message
    }

    model.addAttribute("token", token);
    return "ResetPasswordForm";  // Return the view for resetting the password
}

@PostMapping("/reset_password")
public ResponseEntity<?> processResetPassword(@RequestBody Map<String, String> payload) {
    String token = payload.get("token");
    String newPassword = payload.get("password");

    try {
        User user = userService.get(token);
        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid token");
            return ResponseEntity.badRequest().body(response);
        }

        userService.updatePassword(user, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "You have successfully changed your password");
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "An error occurred while resetting your password");
        return ResponseEntity.internalServerError().body(response);
    }
}

}