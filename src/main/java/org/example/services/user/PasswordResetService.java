package org.example.services.user;

import org.example.model.user.PasswordResetToken;
import org.example.util.DatabaseConnection;
import org.example.model.user.UserAccount;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import javax.mail.*;
import javax.mail.internet.*;

public class PasswordResetService {

    private UserAccountService userService = new UserAccountService();

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // Generate a unique token
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    // Save token to database
    public boolean createResetToken(int userId, String token, LocalDateTime expiry) {
        String sql = "INSERT INTO password_reset_token (user_id, token, expiry_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(expiry));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find token record
    public PasswordResetToken findToken(String token) {
        String sql = "SELECT * FROM password_reset_token WHERE token = ? AND used = FALSE AND expiry_date > ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PasswordResetToken t = new PasswordResetToken();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setToken(rs.getString("token"));
                t.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
                t.setUsed(rs.getBoolean("used"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Mark token as used
    public void invalidateToken(int tokenId) {
        String sql = "UPDATE password_reset_token SET used = TRUE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Send reset email
    public void sendResetEmail(String recipientEmail, String token) {
        // Email configuration – replace with your SMTP settings
        final String username = "medziiko@gmail.com";
        final String password = "vwot uyeg pikt trqr"; // Use app-specific password for Gmail

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset Request");
            message.setText("Your password reset token is: " + token + "\n\nThis token will expire in 15 minutes.");

            Transport.send(message);
            System.out.println("Reset email sent to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    // Complete reset process: validate token and update password
    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = findToken(token);
        if (resetToken == null) {
            return false; // invalid or expired
        }

        // Update password
        userService.changePassword(resetToken.getUserId(), newPassword);
        // Invalidate token
        invalidateToken(resetToken.getId());
        return true;
    }

    // Check if email exists, generate token, save, send email
    public boolean initiatePasswordReset(String email) {
        // Find user by email – need to add method in UserAccountService
        UserAccount user = userService.findByEmail(email); // We'll add this method next
        if (user == null) {
            return false;
        }

        String token = generateToken();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);
        boolean saved = createResetToken(user.getUserId(), token, expiry);
        if (!saved) {
            return false;
        }

        // Send email
        sendResetEmail(email, token);
        return true;
    }
}