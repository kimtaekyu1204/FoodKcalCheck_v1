package com.example.backendspring.repository;

import com.example.backendspring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUniqueCode(String uniqueCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByUniqueCode(String uniqueCode);
}

