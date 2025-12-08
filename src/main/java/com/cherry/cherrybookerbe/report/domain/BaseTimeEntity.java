package com.cherry.cherrybookerbe.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class BaseTimeEntity {

    //신고 시간
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    // 게시물 삭제 여부
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    // 소프트 삭제 메서드
    public void isDeleted(boolean isDeleted) {
        this.isDeleted = true;
    }

}
