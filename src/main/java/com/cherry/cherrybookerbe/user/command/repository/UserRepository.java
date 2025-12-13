package com.cherry.cherrybookerbe.user.command.repository;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;



public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUserEmail(String email);
    Optional<User> findByUserId(Integer userId);
    Optional<User>  findByUserEmailAndUserRole(String email, UserRole role);
    Optional<User> findByUserIdAndUserRole(Integer userId, UserRole role);
    boolean existsByUserEmailAndUserRole(String email, UserRole role);

    // 전체 유저 조회
    @Query("select u.userId from User u")
    List<Integer> findAllUserIds();

}


