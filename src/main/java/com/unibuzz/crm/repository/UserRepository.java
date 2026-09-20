package com.unibuzz.crm.repository;

import com.unibuzz.crm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    java.util.List<User> findByRoles_Name(String roleName);
}
