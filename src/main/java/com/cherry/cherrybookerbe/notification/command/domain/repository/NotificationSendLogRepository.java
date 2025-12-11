package com.cherry.cherrybookerbe.notification.command.domain.repository;

import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationSendLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSendLogRepository extends JpaRepository<NotificationSendLog, Integer> {

    Page<NotificationSendLog> findAllByOrderBySentAtDesc(Pageable pageable);
}
