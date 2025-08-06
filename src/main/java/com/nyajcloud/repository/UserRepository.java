package com.nyajcloud.repository;

import com.nyajcloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}