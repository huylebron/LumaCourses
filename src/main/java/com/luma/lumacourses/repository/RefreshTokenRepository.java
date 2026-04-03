package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.RefreshToken;
import com.luma.lumacourses.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** UUID */
    Optional<RefreshToken> findByJti(String jti);

    /**  SHA-256 hash  */
    Optional<RefreshToken> findByToken(String tokenHash);

    /** Revoke ALL refresh tokens */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(@Param("user") User user);

    /**  delete expired tokens */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
