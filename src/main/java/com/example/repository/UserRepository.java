package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
import com.example.entity.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))")
    boolean existsByEmail(@Param("email") String email);
//    boolean existsByEmail(String email);
    Optional<User>findByfullName(String fullName);
    Optional<User> findByResetToken(String resetToken);

}
