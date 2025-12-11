package com.cherry.cherrybookerbe.notification.command.domain.repository;

import com.cherry.cherrybookerbe.notification.command.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    long countByUserIdAndReadFalse(Integer userId);

    List<Notification> findByUserIdAndReadFalse(Integer userId);

    void deleteByUserIdAndReadTrue(Integer userId);
}
