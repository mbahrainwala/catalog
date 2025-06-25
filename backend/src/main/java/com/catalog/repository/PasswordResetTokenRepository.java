package com.catalog.repository;

import com.catalog.entity.PasswordResetToken;
import com.catalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUserAndUsedFalseOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user = :user AND prt.createdAt > :since")
    long countByUserAndCreatedAtAfter(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user = :user AND prt.used = false")
    void markAllUserTokensAsUsed(@Param("user") User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now OR prt.used = true")
    void deleteExpiredAndUsedTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
}