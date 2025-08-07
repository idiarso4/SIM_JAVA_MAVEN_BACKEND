package com.school.sim.service;

import com.school.sim.entity.User;

/**
 * Email service interface for sending various types of emails
 * Implementation can be done with Spring Mail or external email services
 */
public interface EmailService {

    /**
     * Send password reset email to user
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Send welcome email to new user
     */
    void sendWelcomeEmail(User user, String temporaryPassword);

    /**
     * Send account activation email
     */
    void sendAccountActivationEmail(User user, String activationToken);

    /**
     * Send notification email
     */
    void sendNotificationEmail(User user, String subject, String message);
}
