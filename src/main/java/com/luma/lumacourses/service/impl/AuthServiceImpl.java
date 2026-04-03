package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.util.exception.AccountDisabledException;
import com.luma.lumacourses.util.exception.InvalidTokenException;
import com.luma.lumacourses.dto.auth.*;
import com.luma.lumacourses.entity.RefreshToken;
import com.luma.lumacourses.entity.TokenBlacklist;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.repository.RefreshTokenRepository;
import com.luma.lumacourses.repository.TokenBlacklistRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.jwt.JwtService;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.security.principal.UserPrincipalService;
import com.luma.lumacourses.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;
    private final UserPrincipalService userPrincipalService;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    //  login

    @Override
    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException on bad password, DisabledException on inactive account
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();


        if (!principal.isEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        return buildLoginResponse(principal);
    }

    //  verify

    @Override
    @Transactional(readOnly = true)
    public VerifyResponse verify(VerifyRequest request) {
        try {
            String token = request.token();
            if (!jwtService.validateToken(token)) {
                return new VerifyResponse(false, null, null, null, null);
            }

            Claims claims = jwtService.extractAllClaims(token);

            // Reject blacklisted tokens
            if (jwtService.isTokenBlacklisted(claims.getId())) {
                return new VerifyResponse(false, null, null, null, null);
            }

            return new VerifyResponse(
                    true,
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("role",  String.class),
                    claims.getExpiration().toInstant()
            );
        } catch (Exception e) {
            log.warn("Token verification failed: {}", e.getMessage());
            return new VerifyResponse(false, null, null, null, null);
        }
    }

    //  me

    @Override
    @Transactional(readOnly = true)
    public MeResponse me(UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // logout

    @Override
    public void logout(String rawAccessToken, LogoutRequest request) {
        // Blacklist access token JTI
        try {
            Claims accessClaims = jwtService.extractAllClaims(rawAccessToken);
            String jti = accessClaims.getId();

            if (!jwtService.isTokenBlacklisted(jti)) {
                TokenBlacklist entry = new TokenBlacklist();
                entry.setJti(jti);
                entry.setExpiresAt(accessClaims.getExpiration()
                        .toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
                tokenBlacklistRepository.save(entry);
                log.info("Access token JTI blacklisted: {}", jti);
            }
        } catch (JwtException e) {
            // Access token  expired
            log.warn("Could not parse access token during logout: {}", e.getMessage());
        }

        // 2. Revoke refresh token in DB
        try {
            Claims refreshClaims = jwtService.extractAllClaims(request.refreshToken());
            String refreshJti = refreshClaims.getId();

            refreshTokenRepository.findByJti(refreshJti).ifPresentOrElse(
                    rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                        log.info("Refresh token revoked, JTI: {}", refreshJti);
                    },
                    () -> log.warn("Refresh token not found in DB during logout, JTI: {}", refreshJti)
            );
        } catch (JwtException e) {
            log.warn("Could not parse refresh token during logout: {}", e.getMessage());
        }
    }

    //  refresh

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        String rawRefreshToken = request.refreshToken();

        //  Validate JWT signature
        if (!jwtService.validateToken(rawRefreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Claims claims = jwtService.extractAllClaims(rawRefreshToken);

        // refresh token
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new InvalidTokenException("Provided token is not a refresh token");
        }

        //  Lookup JTI
        String jti = claims.getId();
        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        //  Check revocation
        if (stored.isRevoked()) {
            log.warn("Revoked refresh token reuse detected for user ID: {}. Revoking all tokens.",
                    stored.getUser().getId());
            refreshTokenRepository.revokeAllByUser(stored.getUser());
            throw new InvalidTokenException("Refresh token has been revoked. Please login again.");
        }

        // Check DB expiry
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        //  Hash integrity check
        if (!hashToken(rawRefreshToken).equals(stored.getToken())) {
            throw new InvalidTokenException("Refresh token integrity check failed");
        }

        // revoke old token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        //  Issue new token pair
        UserPrincipal principal = (UserPrincipal) userPrincipalService
                .loadUserByUsername(stored.getUser().getEmail());

        if (!principal.isEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        return buildLoginResponse(principal);
    }

    //  Helpers ─

    private LoginResponse buildLoginResponse(UserPrincipal principal) {
        String accessToken      = jwtService.generateAccessToken(principal);
        String rawRefreshToken  = jwtService.generateRefreshToken(principal);
        String refreshJti       = jwtService.extractJti(rawRefreshToken);

        //  refresh token for security
        RefreshToken entity = new RefreshToken();
        entity.setUser(userRepository.getReferenceById(principal.getUserId()));
        entity.setToken(hashToken(rawRefreshToken));
        entity.setJti(refreshJti);
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new LoginResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                accessTokenExpiryMs / 1000,
                new UserSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                )
        );
    }

    /** SHA-256 hash token string */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
