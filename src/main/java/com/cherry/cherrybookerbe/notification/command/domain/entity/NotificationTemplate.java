package com.cherry.cherrybookerbe.notification.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alarm_template")
public class NotificationTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer id;

    @Column(name = "template_title", nullable = false, length = 50)
    private String title;

    @Column(name = "template_context", nullable = false, length = 255)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 20)
    private NotificationTemplateType type;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationSendLog> sendLogs = new ArrayList<>();

    @Builder
    private NotificationTemplate(String title,
                                 String body,
                                 NotificationTemplateType type) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.deleted = false;
    }

    public void update(String title, String body, NotificationTemplateType type) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (body != null && !body.isBlank()) {
            this.body = body;
        }
        if (type != null) {
            this.type = type;
        }
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public void restore() {
        this.deleted = false;
    }
}
