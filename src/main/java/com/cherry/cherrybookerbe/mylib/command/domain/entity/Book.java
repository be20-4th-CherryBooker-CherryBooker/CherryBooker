package com.cherry.cherrybookerbe.mylib.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book",
        uniqueConstraints = {
            @UniqueConstraint(name = "isbn", columnNames = "isbn")
        }
)
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "author", nullable = false, length = 20)
    private String author;

    @Column(name = "isbn", nullable = false, length = 20)
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
