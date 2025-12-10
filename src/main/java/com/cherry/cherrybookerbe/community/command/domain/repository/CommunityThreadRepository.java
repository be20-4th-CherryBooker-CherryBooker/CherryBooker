package com.cherry.cherrybookerbe.community.command.domain.repository;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityThreadRepository extends JpaRepository<CommunityThread, Integer> {

    // 스레드 목록: 삭제되지 않은 최상위 스레드 (parent가 null)
    List<CommunityThread> findByDeletedFalseAndParentIsNullOrderByCreatedAtDesc();

    // 공통 조회 (루트/릴레이 둘 다 사용)
    Optional<CommunityThread> findByIdAndDeletedFalse(Integer id);

    // 페이징용
    Page<CommunityThread> findByDeletedFalseAndParentIsNull(Pageable pageable);
    
}
