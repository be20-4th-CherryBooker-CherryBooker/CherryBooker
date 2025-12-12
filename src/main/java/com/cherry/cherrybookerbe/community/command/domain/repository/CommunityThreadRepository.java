package com.cherry.cherrybookerbe.community.command.domain.repository;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityThreadRepository extends JpaRepository<CommunityThread, Integer> {

    // 스레드 목록: 삭제되지 않은 최상위 스레드 (parent가 null)
    List<CommunityThread> findByDeletedFalseAndParentIsNullOrderByCreatedAtDesc();

    // 공통 조회 (루트/릴레이 둘 다 사용)
    Optional<CommunityThread> findByIdAndDeletedFalse(Integer id);

    // 페이징용
    Page<CommunityThread> findByDeletedFalseAndParentIsNull(Pageable pageable);

    /* 마이페이지 조회 시 사용 */
    @Query("SELECT COUNT(ct) FROM CommunityThread ct " +
            "WHERE ct.userId = :userId AND ct.deleted = false")
    long countByUserId(@Param("userId") Integer userId);
}
