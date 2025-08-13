package com.nyajcloud.repository;

import com.nyajcloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "utilisateurs")
public interface UserRepository extends JpaRepository<User, Long> {
}