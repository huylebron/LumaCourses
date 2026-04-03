package com.luma.lumacourses.security.config;

import com.luma.lumacourses.security.jwt.JwtService;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.security.principal.UserPrincipalService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserPrincipalService userPrincipalService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractBearerToken(request);

            if (StringUtils.hasText(token) && jwtService.validateToken(token)) {
                Claims claims = jwtService.extractAllClaims(token);


                String tokenType = claims.get("type", String.class);
                if (!"access".equals(tokenType)) {
                    log.warn("Refresh token used as access token on path: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Check  logout token
                String jti = claims.getId();
                if (jwtService.isTokenBlacklisted(jti)) {
                    log.warn("Blacklisted JTI used: {} on path: {}", jti, request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Load fresh user state
                String email = claims.get("email", String.class);
                UserPrincipal principal = (UserPrincipal) userPrincipalService.loadUserByUsername(email);

                if (!principal.isEnabled()) {
                    log.warn("Disabled account attempted access: {}", email);
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Failed to set authentication from JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
