package com.hvac.iot.service;

import com.hvac.iot.dto.*;
import com.hvac.iot.model.*;
import com.hvac.iot.repository.*;
import com.hvac.iot.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SuperAdminRepository superAdminRepository;
    private final DeviceOwnerRepository deviceOwnerRepository;
    private final DeviceUserRepository deviceUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        String password = request.getPassword();

        // Check SuperAdmin
        var superAdmin = superAdminRepository.findByEmail(email);
        if (superAdmin.isPresent() && passwordEncoder.matches(password, superAdmin.get().getPasswordHash())) {
            SuperAdmin admin = superAdmin.get();
            String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), "SUPER_ADMIN", admin.getName());
            return LoginResponse.builder()
                    .token(token)
                    .role("SUPER_ADMIN")
                    .userId(admin.getId().toString())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .build();
        }

        // Check DeviceOwner
        var deviceOwner = deviceOwnerRepository.findByEmail(email);
        if (deviceOwner.isPresent() && passwordEncoder.matches(password, deviceOwner.get().getPasswordHash())) {
            DeviceOwner owner = deviceOwner.get();
            String token = jwtUtil.generateToken(owner.getId(), owner.getEmail(), "DEVICE_OWNER", owner.getName());
            return LoginResponse.builder()
                    .token(token)
                    .role("DEVICE_OWNER")
                    .userId(owner.getId().toString())
                    .name(owner.getName())
                    .email(owner.getEmail())
                    .build();
        }

        // Check DeviceUser
        var deviceUser = deviceUserRepository.findByEmail(email);
        if (deviceUser.isPresent() && passwordEncoder.matches(password, deviceUser.get().getPasswordHash())) {
            DeviceUser user = deviceUser.get();
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "DEVICE_USER", user.getName());
            return LoginResponse.builder()
                    .token(token)
                    .role("DEVICE_USER")
                    .userId(user.getId().toString())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
        }

        throw new RuntimeException("Invalid email or password");
    }

    public LoginResponse refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        String userId = jwtUtil.extractUserId(token);
        String name = jwtUtil.extractName(token);

        String newToken = jwtUtil.generateToken(UUID.fromString(userId), email, role, name);

        return LoginResponse.builder()
                .token(newToken)
                .role(role)
                .userId(userId)
                .name(name)
                .email(email)
                .build();
    }

    @Transactional
    public void requestPasswordReset(String email) {
        email = email.toLowerCase();
        String userType = null;

        if (superAdminRepository.existsByEmail(email)) {
            userType = "SUPER_ADMIN";
        } else if (deviceOwnerRepository.existsByEmail(email)) {
            userType = "DEVICE_OWNER";
        } else if (deviceUserRepository.existsByEmail(email)) {
            userType = "DEVICE_USER";
        }

        if (userType == null) {
            // Don't reveal whether the email exists
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .userType(userType)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email (in production, this would send an actual email)
        emailService.sendPasswordResetEmail(email, token);
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Reset token has expired");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        String email = resetToken.getEmail();

        switch (resetToken.getUserType()) {
            case "SUPER_ADMIN" -> superAdminRepository.findByEmail(email)
                    .ifPresent(admin -> admin.setPasswordHash(encodedPassword));
            case "DEVICE_OWNER" -> deviceOwnerRepository.findByEmail(email)
                    .ifPresent(owner -> owner.setPasswordHash(encodedPassword));
            case "DEVICE_USER" -> deviceUserRepository.findByEmail(email)
                    .ifPresent(user -> user.setPasswordHash(encodedPassword));
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
