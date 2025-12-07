package com.cherry.cherrybookerbe.mylib.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "author", nullable = false, length = 20)
    private String author;

    @Column(name = "ISBN", nullable = false, length = 20)
    private String isbn;

    @Column(name = "cover_image_url", nullable = false, length = 500)
    private String coverImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Book(String title, String author, String isbn, String coverImageUrl) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
