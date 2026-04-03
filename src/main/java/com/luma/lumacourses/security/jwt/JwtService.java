package com.luma.lumacourses.security.jwt;

import com.luma.lumacourses.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;

public interface JwtService {

    /** Generate  access token 15p*/
    String generateAccessToken(UserPrincipal principal);

    /** Generate long refresh token 7 days.*/
    String generateRefreshToken(UserPrincipal principal);

    /** Validate signature + expired */
    boolean validateToken(String token);


    Claims extractAllClaims(String token);

    /** Extract jwt id  */
    String extractJti(String token);

    /** Extract user id */
    Long extractUserId(String token);

    /** Check if the access token JTI exists in the token_blacklist table */
    boolean isTokenBlacklisted(String jti);
}
