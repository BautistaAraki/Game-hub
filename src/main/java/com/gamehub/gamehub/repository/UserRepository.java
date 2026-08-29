package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.mode1.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}