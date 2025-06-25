package com.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${catalog.mail.from}")
    private String fromEmail;
    
    @Value("${catalog.mail.from-name}")
    private String fromName;
    
    @Value("${catalog.app.base-url}")
    private String baseUrl;
    
    public void sendAccountActivationEmail(String toEmail, String firstName, String username, String temporaryPassword) {
        try {
            logger.info("Sending account activation email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Account Created - Activate Your Industrial Catalog Account");
            
            // Create email content
            String htmlContent = createAccountActivationEmailContent(firstName, username, temporaryPassword);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Account activation email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to create account activation email for: {}", toEmail, e);
            throw new RuntimeException("Failed to create email", e);
        } catch (MailException e) {
            logger.error("Failed to send account activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending account activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendAccountActivatedEmail(String toEmail, String firstName) {
        try {
            logger.info("Sending account activated confirmation email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Account Activated - Welcome to Industrial Catalog");
            
            // Create email content
            String htmlContent = createAccountActivatedEmailContent(firstName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Account activated email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send account activated email to: {}", toEmail, e);
            // Don't throw exception here as account activation was successful
        }
    }
    
    private String createAccountActivationEmailContent(String firstName, String username, String temporaryPassword) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Created - Activate Your Account</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2563eb;
                        margin-bottom: 10px;
                    }
                    .welcome-icon {
                        font-size: 48px;
                        margin-bottom: 20px;
                    }
                    .title {
                        font-size: 28px;
                        font-weight: bold;
                        color: #1f2937;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        color: #6b7280;
                        font-size: 16px;
                    }
                    .content {
                        margin: 30px 0;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 30px;
                        line-height: 1.7;
                    }
                    .credentials-box {
                        background-color: #f0f9ff;
                        border: 2px solid #0ea5e9;
                        border-radius: 12px;
                        padding: 25px;
                        margin: 30px 0;
                        text-align: center;
                    }
                    .credentials-title {
                        font-weight: 600;
                        color: #0c4a6e;
                        margin-bottom: 15px;
                        font-size: 18px;
                    }
                    .credential-item {
                        margin: 10px 0;
                        padding: 10px;
                        background-color: white;
                        border-radius: 8px;
                        border: 1px solid #bae6fd;
                    }
                    .credential-label {
                        font-weight: 600;
                        color: #0c4a6e;
                        font-size: 14px;
                    }
                    .credential-value {
                        font-family: 'Courier New', monospace;
                        font-size: 16px;
                        color: #1e40af;
                        font-weight: bold;
                        margin-top: 5px;
                    }
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                    }
                    .activate-button {
                        display: inline-block;
                        background-color: #059669;
                        color: white;
                        padding: 16px 32px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 16px;
                        transition: background-color 0.3s;
                    }
                    .activate-button:hover {
                        background-color: #047857;
                    }
                    .warning {
                        background-color: #fef3c7;
                        border: 1px solid #f59e0b;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .warning-title {
                        font-weight: 600;
                        color: #92400e;
                        margin-bottom: 10px;
                        display: flex;
                        align-items: center;
                    }
                    .warning-text {
                        color: #92400e;
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    .steps {
                        background-color: #f3f4f6;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .steps-title {
                        font-weight: 600;
                        color: #374151;
                        margin-bottom: 15px;
                    }
                    .step {
                        margin: 10px 0;
                        padding-left: 20px;
                        position: relative;
                    }
                    .step::before {
                        content: counter(step-counter);
                        counter-increment: step-counter;
                        position: absolute;
                        left: 0;
                        top: 0;
                        background-color: #2563eb;
                        color: white;
                        width: 18px;
                        height: 18px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                        font-weight: bold;
                    }
                    .steps {
                        counter-reset: step-counter;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #e5e7eb;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏭 Industrial Catalog</div>
                        <div class="welcome-icon">🎉</div>
                        <h1 class="title">Welcome to Industrial Catalog!</h1>
                        <p class="subtitle">Your account has been created successfully</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Hello %s,</p>
                        
                        <div class="message">
                            <p>Welcome to Industrial Catalog! Your account has been created successfully. To ensure the security of your account, we've generated a temporary password for you.</p>
                            <p><strong>You must activate your account within 48 hours</strong> by logging in with your temporary password and setting up a new secure password.</p>
                        </div>
                        
                        <div class="credentials-box">
                            <div class="credentials-title">🔐 Your Login Credentials</div>
                            <div class="credential-item">
                                <div class="credential-label">Username:</div>
                                <div class="credential-value">%s</div>
                            </div>
                            <div class="credential-item">
                                <div class="credential-label">Temporary Password:</div>
                                <div class="credential-value">%s</div>
                            </div>
                        </div>
                        
                        <div class="button-container">
                            <a href="%s" class="activate-button">Activate Your Account</a>
                        </div>
                        
                        <div class="steps">
                            <div class="steps-title">📋 How to Activate Your Account:</div>
                            <div class="step">Click the "Activate Your Account" button above</div>
                            <div class="step">Log in using your username and temporary password</div>
                            <div class="step">Create a new secure password</div>
                            <div class="step">Start exploring our industrial equipment catalog!</div>
                        </div>
                        
                        <div class="warning">
                            <div class="warning-title">⚠️ Important Security Information</div>
                            <div class="warning-text">
                                • Your account will be automatically deleted if not activated within 48 hours<br>
                                • Change your password immediately after first login<br>
                                • Never share your login credentials with anyone<br>
                                • Keep this email secure until you've activated your account
                            </div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent from Industrial Catalog</p>
                        <p>Your trusted partner for drilling and mining equipment</p>
                        <p style="margin-top: 15px; font-size: 12px;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, username, temporaryPassword, baseUrl);
    }
    
    private String createAccountActivatedEmailContent(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Activated Successfully</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2563eb;
                        margin-bottom: 10px;
                    }
                    .success-icon {
                        font-size: 48px;
                        margin-bottom: 20px;
                    }
                    .title {
                        font-size: 28px;
                        font-weight: bold;
                        color: #059669;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        color: #6b7280;
                        font-size: 16px;
                    }
                    .content {
                        margin: 30px 0;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 30px;
                        line-height: 1.7;
                    }
                    .features {
                        background-color: #f0f9ff;
                        border: 1px solid #0ea5e9;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .features-title {
                        font-weight: 600;
                        color: #0c4a6e;
                        margin-bottom: 15px;
                    }
                    .feature-list {
                        color: #0c4a6e;
                        font-size: 14px;
                        margin: 0;
                        padding-left: 20px;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .login-button {
                        display: inline-block;
                        background-color: #2563eb;
                        color: white;
                        padding: 16px 32px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 16px;
                        transition: background-color 0.3s;
                    }
                    .login-button:hover {
                        background-color: #1d4ed8;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #e5e7eb;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏭 Industrial Catalog</div>
                        <div class="success-icon">✅</div>
                        <h1 class="title">Account Activated!</h1>
                        <p class="subtitle">Welcome to Industrial Catalog</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Hello %s,</p>
                        
                        <div class="message">
                            <p>Congratulations! Your Industrial Catalog account has been successfully activated. You can now access our full range of drilling and mining equipment with your new secure password.</p>
                        </div>
                        
                        <div class="features">
                            <div class="features-title">🚀 What you can do now:</div>
                            <ul class="feature-list">
                                <li>Browse our extensive catalog of industrial equipment</li>
                                <li>Use advanced search and filtering options</li>
                                <li>Access detailed product specifications</li>
                                <li>Contact our team for quotes and support</li>
                                <li>Manage your account settings and preferences</li>
                            </ul>
                        </div>
                        
                        <div class="button-container">
                            <a href="%s" class="login-button">Start Exploring</a>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent from Industrial Catalog</p>
                        <p>Your trusted partner for drilling and mining equipment</p>
                        <p style="margin-top: 15px; font-size: 12px;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, baseUrl);
    }
    
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        try {
            logger.info("Sending password reset email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Industrial Catalog");
            
            // Create email content
            String htmlContent = createPasswordResetEmailContent(userName, resetLink);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to create password reset email for: {}", toEmail, e);
            throw new RuntimeException("Failed to create email", e);
        } catch (MailException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String createPasswordResetEmailContent(String userName, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Request</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2563eb;
                        margin-bottom: 10px;
                    }
                    .title {
                        font-size: 28px;
                        font-weight: bold;
                        color: #1f2937;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        color: #6b7280;
                        font-size: 16px;
                    }
                    .content {
                        margin: 30px 0;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 30px;
                        line-height: 1.7;
                    }
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                    }
                    .reset-button {
                        display: inline-block;
                        background-color: #2563eb;
                        color: white;
                        padding: 16px 32px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 16px;
                        transition: background-color 0.3s;
                    }
                    .reset-button:hover {
                        background-color: #1d4ed8;
                    }
                    .alternative-link {
                        margin-top: 20px;
                        padding: 15px;
                        background-color: #f3f4f6;
                        border-radius: 8px;
                        word-break: break-all;
                        font-size: 14px;
                        color: #6b7280;
                    }
                    .warning {
                        background-color: #fef3c7;
                        border: 1px solid #f59e0b;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .warning-title {
                        font-weight: 600;
                        color: #92400e;
                        margin-bottom: 5px;
                    }
                    .warning-text {
                        color: #92400e;
                        font-size: 14px;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #e5e7eb;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                    }
                    .help-text {
                        margin-top: 20px;
                        font-size: 14px;
                        color: #6b7280;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏭 Industrial Catalog</div>
                        <h1 class="title">Password Reset Request</h1>
                        <p class="subtitle">Secure access to your account</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Hello %s,</p>
                        
                        <div class="message">
                            <p>We received a request to reset the password for your Industrial Catalog account. If you made this request, click the button below to create a new password.</p>
                        </div>
                        
                        <div class="button-container">
                            <a href="%s" class="reset-button">Reset Your Password</a>
                        </div>
                        
                        <div class="alternative-link">
                            <strong>Can't click the button?</strong> Copy and paste this link into your browser:<br>
                            <span style="color: #2563eb;">%s</span>
                        </div>
                        
                        <div class="warning">
                            <div class="warning-title">⚠️ Important Security Information</div>
                            <div class="warning-text">
                                • This link will expire in 24 hours for your security<br>
                                • If you didn't request this reset, please ignore this email<br>
                                • Never share this link with anyone
                            </div>
                        </div>
                        
                        <div class="help-text">
                            <p><strong>Need help?</strong> If you're having trouble resetting your password or didn't request this change, please contact our support team.</p>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent from Industrial Catalog</p>
                        <p>Your trusted partner for drilling and mining equipment</p>
                        <p style="margin-top: 15px; font-size: 12px;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, resetLink, resetLink);
    }
    
    public void sendPasswordResetConfirmationEmail(String toEmail, String userName) {
        try {
            logger.info("Sending password reset confirmation email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Password Successfully Reset - Industrial Catalog");
            
            // Create email content
            String htmlContent = createPasswordResetConfirmationContent(userName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Password reset confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset confirmation email to: {}", toEmail, e);
            // Don't throw exception here as password reset was successful
        }
    }
    
    private String createPasswordResetConfirmationContent(String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Successful</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2563eb;
                        margin-bottom: 10px;
                    }
                    .success-icon {
                        font-size: 48px;
                        margin-bottom: 20px;
                    }
                    .title {
                        font-size: 28px;
                        font-weight: bold;
                        color: #059669;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        color: #6b7280;
                        font-size: 16px;
                    }
                    .content {
                        margin: 30px 0;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 30px;
                        line-height: 1.7;
                    }
                    .security-tips {
                        background-color: #f0f9ff;
                        border: 1px solid #0ea5e9;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .security-title {
                        font-weight: 600;
                        color: #0c4a6e;
                        margin-bottom: 10px;
                    }
                    .security-list {
                        color: #0c4a6e;
                        font-size: 14px;
                        margin: 0;
                        padding-left: 20px;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #e5e7eb;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏭 Industrial Catalog</div>
                        <div class="success-icon">✅</div>
                        <h1 class="title">Password Reset Successful</h1>
                        <p class="subtitle">Your account is now secure</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Hello %s,</p>
                        
                        <div class="message">
                            <p>Your password has been successfully reset. You can now log in to your Industrial Catalog account using your new password.</p>
                            <p>If you did not make this change, please contact our support team immediately.</p>
                        </div>
                        
                        <div class="security-tips">
                            <div class="security-title">🔒 Security Tips</div>
                            <ul class="security-list">
                                <li>Use a strong, unique password for your account</li>
                                <li>Never share your password with anyone</li>
                                <li>Log out of shared or public computers</li>
                                <li>Contact us if you notice any suspicious activity</li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent from Industrial Catalog</p>
                        <p>Your trusted partner for drilling and mining equipment</p>
                        <p style="margin-top: 15px; font-size: 12px;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }
}