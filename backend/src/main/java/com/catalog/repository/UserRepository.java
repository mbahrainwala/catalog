package com.catalog.repository;

import com.catalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.accountActivated = false AND u.isTemporaryPassword = true AND u.activationDeadline < :now")
    List<User> findExpiredUnactivatedUsers(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.accountActivated = false AND u.isTemporaryPassword = true")
    Optional<User> findUnactivatedUserByEmail(@Param("email") String email);
}