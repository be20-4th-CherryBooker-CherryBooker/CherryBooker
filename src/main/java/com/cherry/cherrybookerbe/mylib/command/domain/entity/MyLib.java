package com.cherry.cherrybookerbe.mylib.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 엔티티를 마음대로 new로 만들지 못하게 막고, 대신 JPA 내부 동작(프록시, 리플렉션)이 필요한 곳에서만 생성할 수 있게 하려는 목적
@Entity
@Table(name = "my_lib")
public class MyLib extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "my_lib_id")
    private Long myLibId;

    // 보수적인 관점의 프로그래밍: 불필요한 데이터 로딩을 미루기 위해 일단 fetch type을 lazy로 설정
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status", nullable = false, length = 10)
    private BookStatus bookStatus;

    @Builder
    public MyLib(User user, Book book, BookStatus bookStatus) {
        this.user = user;
        this.book = book;
        if (bookStatus != null) {
            this.bookStatus = bookStatus;
        }
    }

    public void changeBookStatus(BookStatus bookStatus) {
        this.bookStatus = bookStatus;
    }

    @PrePersist
    protected void onCreate() {
        if (bookStatus == null) {
            bookStatus = BookStatus.WISH;      // entity 생성시 초기화 상태는 WISH (읽고 싶은 책)
        }
    }
}
