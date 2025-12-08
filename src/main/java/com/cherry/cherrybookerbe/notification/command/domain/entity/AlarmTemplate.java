package com.cherry.cherrybookerbe.notification.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import com.cherry.cherrybookerbe.notification.command.domain.enums.TemplateType;
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
public class AlarmTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    @Column(name = "template_title", nullable = false, length = 50)
    private String templateTitle;

    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 10)
    private TemplateType templateType;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<AlarmSendLog> sendLogs = new ArrayList<>();

    @Builder
    private AlarmTemplate(Integer userId,
                          String templateTitle,
                          String templateContext,
                          TemplateType templateType) {
        this.userId = userId;
        this.templateTitle = templateTitle;
        this.templateContext = templateContext;
        this.templateType = templateType != null ? templateType : TemplateType.SYSTEM;
    }

    public void markDeleted() {
        this.deleted = true;
    }
}
