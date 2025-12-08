package com.cherry.cherrybookerbe.mylib.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book")
public class Book extends BaseTimeEntity {

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

    @Builder
    public Book(String title, String author, String isbn, String coverImageUrl) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
    }
}
