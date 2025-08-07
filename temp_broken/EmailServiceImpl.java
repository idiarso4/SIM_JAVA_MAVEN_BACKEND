package com.school.sim.service.impl;

import com.school.sim.entity.User;
import com.school.sim.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Basic implementation of EmailService
 * This is a placeholder implementation that logs email actions
 * In production, this should be replaced with actual email sending logic
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@schoolsim.com}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        
        logger.info("Sending password reset email to: {}", user.getEmail());
        logger.info("Reset URL: {}", resetUrl);
        
        // TODO: Implement actual email sending logic
        // This could use Spring Mail, SendGrid, AWS SES, etc.
        
        String subject = "Password Reset Request - School SIM";
        String message = String.format(
            "Dear %s,\n\n" +
            "You have requested to reset your password for School SIM.\n\n" +
            "Please click the following link to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "School SIM Team",
            user.getName(),
            resetUrl
        );
        
        logger.debug("Email content - Subject: {}, Message: {}", subject, message);
    }

    @Override
    public void sendWelcomeEmail(User user, String temporaryPassword) {
        logger.info("Sending welcome email to: {}", user.getEmail());
        
        String subject = "Welcome to School SIM";
        String message = String.format(
            "Dear %s,\n\n" +
            "Welcome to School SIM! Your account has been created successfully.\n\n" +
            "Your login credentials:\n" +
            "Email: %s\n" +
            "Temporary Password: %s\n\n" +
            "Please log in and change your password immediately.\n\n" +
            "Login URL: %s/login\n\n" +
            "Best regards,\n" +
            "School SIM Team",
            user.getName(),
            user.getEmail(),
            temporaryPassword,
            frontendUrl
        );
        
        logger.debug("Email content - Subject: {}, Message: {}", subject, message);
    }

    @Override
    public void sendAccountActivationEmail(User user, String activationToken) {
        String activationUrl = frontendUrl + "/activate-account?token=" + activationToken;
        
        logger.info("Sending account activation email to: {}", user.getEmail());
        
        String subject = "Account Activation - School SIM";
        String message = String.format(
            "Dear %s,\n\n" +
            "Please activate your School SIM account by clicking the following link:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "Best regards,\n" +
            "School SIM Team",
            user.getName(),
            activationUrl
        );
        
        logger.debug("Email content - Subject: {}, Message: {}", subject, message);
    }

    @Override
    public void sendNotificationEmail(User user, String subject, String message) {
        logger.info("Sending notification email to: {} with subject: {}", user.getEmail(), subject);
        
        String fullMessage = String.format(
            "Dear %s,\n\n" +
            "%s\n\n" +
            "Best regards,\n" +
            "School SIM Team",
            user.getName(),
            message
        );
        
        logger.debug("Email content - Subject: {}, Message: {}", subject, fullMessage);
    }
}
