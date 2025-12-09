package com.cherry.cherrybookerbe.user.command.repository;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUserEmail(String email);
    Optional<User> findByUserId(Integer userId);

}


