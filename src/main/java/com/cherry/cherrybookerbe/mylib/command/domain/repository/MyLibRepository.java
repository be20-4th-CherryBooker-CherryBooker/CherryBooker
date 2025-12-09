package com.cherry.cherrybookerbe.mylib.command.domain.repository;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyLibRepository extends JpaRepository<MyLib, Long> {

    Optional<MyLib> findByUserUserIdAndBookBookId(Integer userId, Long bookId);

    @EntityGraph(attributePaths = "book")
    Optional<MyLib> findWithBookByMyLibId(Long myLibId);
}
