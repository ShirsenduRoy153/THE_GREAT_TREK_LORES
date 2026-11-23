package com.thegreattreklores.auth.service;

import com.thegreattreklores.auth.dto.AuthResponse;
import com.thegreattreklores.auth.dto.LoginRequest;
import com.thegreattreklores.auth.dto.RegisterRequest;
import com.thegreattreklores.auth.jwt.JwtTokenProvider;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.entity.UserProfile;
import com.thegreattreklores.user.enums.AccountStatus;
import com.thegreattreklores.user.enums.UserRole;
import com.thegreattreklores.user.repository.UserProfileRepository;
import com.thegreattreklores.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(request.getDisplayName().trim())
                .build();

        userProfileRepository.save(profile);

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(profile.getDisplayName())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is " + user.getAccountStatus().name().toLowerCase());
        }

        String displayName = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::getDisplayName)
                .orElse(user.getEmail());

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(displayName)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String displayName = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::getDisplayName)
                .orElse(user.getEmail());

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(displayName)
                .build();
    }
}
