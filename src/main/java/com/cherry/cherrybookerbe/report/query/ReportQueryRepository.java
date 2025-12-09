package com.cherry.cherrybookerbe.report.query;

import com.cherry.cherrybookerbe.report.domain.Report;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// 5회 이상 신고 받은 글 목록(pending상태) 조회
public interface ReportQueryRepository extends JpaRepository<Report, Long> {

    @Query("""
            SELECT r.threads.id
            FROM Report r
            WHERE r.status = 'PENDING'
            AND r.threads IS NOT NULL
            GROUP BY r.threads.id
            HAVING COUNT(r.reportId) >=5
            """)
    List<Long> findPendingThreadIdsReportedOverFive();

    @Query("""
        SELECT r.threadsReply.id
        FROM Report r
        WHERE r.status = 'PENDING'
        AND r.threadsReply IS NOT NULL
        GROUP BY r.threadsReply.id
        HAVING COUNT(r.reportId) >= 5
    """)
    List<Long> findPendingReplyIdsReportedOverFive();

}
