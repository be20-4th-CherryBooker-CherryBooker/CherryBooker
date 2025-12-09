package com.cherry.cherrybookerbe.user.command.domain.repository;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
