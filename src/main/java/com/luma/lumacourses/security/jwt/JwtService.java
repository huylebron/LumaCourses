package com.luma.lumacourses.security.jwt;

import com.luma.lumacourses.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;

public interface JwtService {

    /** Generate  access token (15 min) */
    String generateAccessToken(UserPrincipal principal);

    /** Generate long refresh token (7 days).*/
    String generateRefreshToken(UserPrincipal principal);

    /** Validate signature + expired */
    boolean validateToken(String token);

    /** Parse and return all claims */
    Claims extractAllClaims(String token);

    /** Extract jti (JWT ID) claim */
    String extractJti(String token);

    /** Extract subject as Long userId */
    Long extractUserId(String token);

    /** Check if the access token JTI exists in the token_blacklist table */
    boolean isTokenBlacklisted(String jti);
}
