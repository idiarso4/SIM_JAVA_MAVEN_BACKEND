package com.school.sim.service.impl;

import com.school.sim.entity.User;
import com.school.sim.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced implementation of EmailService
 * For development purposes - logs email content with better formatting
 * Can be easily extended to use actual email providers like SendGrid, AWS SES, etc.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.name:School Information Management System}")
    private String applicationName;

    @Value("${app.url:http://localhost:8080}")
    private String applicationUrl;

    @Value("${app.support.email:support@school.edu}")
    private String supportEmail;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request - " + applicationName;
        String resetUrl = applicationUrl + "/reset-password?token=" + resetToken;
        
        String htmlContent = buildPasswordResetEmailContent(user, resetToken, resetUrl);
        
        // Log email details
        logEmailSent("PASSWORD_RESET", user.getEmail(), subject, htmlContent);
        
        // Store email record for tracking
        storeEmailRecord(user, subject, htmlContent, "PASSWORD_RESET");
        
        // In production, implement actual email sending logic here
        // Example: emailProvider.send(user.getEmail(), subject, htmlContent);
        
        log.info("Password reset email queued for user: {} ({})", user.getUsername(), user.getEmail());
    }

    @Override
    public void sendWelcomeEmail(User user, String temporaryPassword) {
        String subject = "Welcome to " + applicationName;
        String loginUrl = applicationUrl + "/login";
        
        String htmlContent = buildWelcomeEmailContent(user, temporaryPassword, loginUrl);
        
        // Log email details
        logEmailSent("WELCOME", user.getEmail(), subject, htmlContent);
        
        // Store email record for tracking
        storeEmailRecord(user, subject, htmlContent, "WELCOME");
        
        // In production, implement actual email sending logic here
        
        log.info("Welcome email queued for user: {} ({})", user.getUsername(), user.getEmail());
    }

    @Override
    public void sendAccountActivationEmail(User user, String activationToken) {
        String subject = "Account Activation Required - " + applicationName;
        String activationUrl = applicationUrl + "/activate?token=" + activationToken;
        
        String htmlContent = buildActivationEmailContent(user, activationToken, activationUrl);
        
        // Log email details
        logEmailSent("ACTIVATION", user.getEmail(), subject, htmlContent);
        
        // Store email record for tracking
        storeEmailRecord(user, subject, htmlContent, "ACTIVATION");
        
        log.info("Account activation email queued for user: {} ({})", user.getUsername(), user.getEmail());
    }

    @Override
    public void sendNotificationEmail(User user, String subject, String message) {
        String fullSubject = subject + " - " + applicationName;
        String htmlContent = buildNotificationEmailContent(user, message);
        
        // Log email details
        logEmailSent("NOTIFICATION", user.getEmail(), fullSubject, htmlContent);
        
        // Store email record for tracking
        storeEmailRecord(user, fullSubject, htmlContent, "NOTIFICATION");
        
        log.info("Notification email queued for user: {} ({})", user.getUsername(), user.getEmail());
    }

    // Helper methods for building email content
    private String buildPasswordResetEmailContent(User user, String resetToken, String resetUrl) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Password Reset Request</h2>
                    <p>Dear %s,</p>
                    <p>We received a request to reset your password for your %s account.</p>
                    <p>Please click the link below to reset your password:</p>
                    <p><a href="%s" style="background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                    <p>Or use this token: <strong>%s</strong></p>
                    <p>This link will expire in 24 hours for security reasons.</p>
                    <p>If you didn't request this password reset, please ignore this email.</p>
                    <hr style="margin: 20px 0;">
                    <p style="font-size: 12px; color: #666;">
                        This email was sent from %s<br>
                        If you need help, contact us at %s
                    </p>
                </div>
            </body>
            </html>
            """, user.getFirstName() + " " + user.getLastName(), applicationName, 
                resetUrl, resetToken, applicationName, supportEmail);
    }

    private String buildWelcomeEmailContent(User user, String temporaryPassword, String loginUrl) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #27ae60;">Welcome to %s!</h2>
                    <p>Dear %s,</p>
                    <p>Welcome to our school information management system. Your account has been created successfully.</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>Your Login Credentials:</h3>
                        <p><strong>Username:</strong> %s</p>
                        <p><strong>Temporary Password:</strong> %s</p>
                    </div>
                    <p><a href="%s" style="background-color: #27ae60; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Login Now</a></p>
                    <p><strong>Important:</strong> Please change your password after your first login for security reasons.</p>
                    <hr style="margin: 20px 0;">
                    <p style="font-size: 12px; color: #666;">
                        This email was sent from %s<br>
                        If you need help, contact us at %s
                    </p>
                </div>
            </body>
            </html>
            """, applicationName, user.getFirstName() + " " + user.getLastName(), 
                user.getUsername(), temporaryPassword, loginUrl, applicationName, supportEmail);
    }

    private String buildActivationEmailContent(User user, String activationToken, String activationUrl) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #e74c3c;">Account Activation Required</h2>
                    <p>Dear %s,</p>
                    <p>Thank you for registering with %s. To complete your registration, please activate your account.</p>
                    <p><a href="%s" style="background-color: #e74c3c; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Activate Account</a></p>
                    <p>Or use this activation token: <strong>%s</strong></p>
                    <p>This activation link will expire in 48 hours.</p>
                    <hr style="margin: 20px 0;">
                    <p style="font-size: 12px; color: #666;">
                        This email was sent from %s<br>
                        If you need help, contact us at %s
                    </p>
                </div>
            </body>
            </html>
            """, user.getFirstName() + " " + user.getLastName(), applicationName, 
                activationUrl, activationToken, applicationName, supportEmail);
    }

    private String buildNotificationEmailContent(User user, String message) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Notification</h2>
                    <p>Dear %s,</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <hr style="margin: 20px 0;">
                    <p style="font-size: 12px; color: #666;">
                        This email was sent from %s<br>
                        If you need help, contact us at %s
                    </p>
                </div>
            </body>
            </html>
            """, user.getFirstName() + " " + user.getLastName(), message, applicationName, supportEmail);
    }

    private void logEmailSent(String type, String recipient, String subject, String content) {
        log.info("=== EMAIL SENT ===");
        log.info("Type: {}", type);
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Timestamp: {}", LocalDateTime.now().format(formatter));
        log.info("Content Length: {} characters", content.length());
        log.info("==================");
    }

    private void storeEmailRecord(User user, String subject, String content, String type) {
        // In a real implementation, you would store this in a database table
        // for email tracking, delivery status, etc.
        Map<String, Object> emailRecord = new HashMap<>();
        emailRecord.put("userId", user.getId());
        emailRecord.put("recipient", user.getEmail());
        emailRecord.put("subject", subject);
        emailRecord.put("type", type);
        emailRecord.put("sentAt", LocalDateTime.now());
        emailRecord.put("status", "QUEUED"); // QUEUED, SENT, DELIVERED, FAILED
        
        // For now, just log that we would store this record
        log.debug("Email record created: {}", emailRecord);
    }
}