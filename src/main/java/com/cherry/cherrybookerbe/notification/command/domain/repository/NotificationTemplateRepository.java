package com.cherry.cherrybookerbe.notification.command.domain.repository;

import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {

    Page<NotificationTemplate> findByDeletedFalse(Pageable pageable);

    Page<NotificationTemplate> findByDeletedFalseAndTitleContainingIgnoreCase(
            String title, Pageable pageable
    );

    Optional<NotificationTemplate> findByTypeAndDeletedFalse(NotificationTemplateType type);
}
