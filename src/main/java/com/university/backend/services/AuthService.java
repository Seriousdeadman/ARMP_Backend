package com.university.backend.services;

import com.university.backend.dto.*;
import com.university.backend.entities.*;
import com.university.backend.enums.UserRole;
import com.university.backend.repositories.*;
import com.university.backend.utils.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MonitoringService monitoringService;
    private final UserAgentParser userAgentParser;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(request.getRole() != null ? request.getRole() : UserRole.STUDENT)
                .isActive(true)
                .totalLoginCount(0)
                .isTwoFactorEnabled(false)
                .build();

        userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .build();
        userProfileRepository.save(profile);

        UserStatistics statistics = UserStatistics.builder()
                .user(user)
                .totalLoginCount(0)
                .totalHoursConnected(0.0)
                .averageSessionDuration(0.0)
                .totalDevicesUsed(0)
                .build();
        userStatisticsRepository.save(statistics);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(AuthRequest request, HttpServletRequest httpRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        user.setTotalLoginCount(user.getTotalLoginCount() + 1);
        userRepository.save(user);

        Session session = Session.builder()
                .user(user)
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .deviceType(userAgentParser.extractDeviceType(httpRequest.getHeader("User-Agent")))
                .browser(userAgentParser.extractBrowser(httpRequest.getHeader("User-Agent")))
                .operatingSystem(userAgentParser.extractOperatingSystem(httpRequest.getHeader("User-Agent")))
                .isActive(true)
                .build();
        sessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public void logout(String userId, String sessionId) {

        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setIsActive(false);
            session.setLogoutAt(LocalDateTime.now());
            if (session.getLoginAt() != null) {
                long minutes = ChronoUnit.MINUTES.between(
                        session.getLoginAt(), session.getLogoutAt()
                );
                session.setDurationMinutes(minutes);
            }
            sessionRepository.save(session);
        });

        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });
        monitoringService.updateStatisticsForUser(userId);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken stored = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (stored.getIsRevoked() || stored.isExpired()) {
            throw new RuntimeException("Refresh token is invalid or expired");
        }

        User user = stored.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);

        saveRefreshToken(user, newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(User user, String tokenString) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(User user,
                                           String accessToken,
                                           String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}