package com.cherry.cherrybookerbe.report.query;

import com.cherry.cherrybookerbe.report.domain.Report;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import com.cherry.cherrybookerbe.report.query.dto.ReportPendingResponse;
import com.cherry.cherrybookerbe.report.query.dto.ReportSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 5회 이상 신고 받은 목록 조회
@Service
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportQueryRepository reportQueryRepository;
    private final JdbcTemplate jdbcTemplate;

    public ReportQueryService(ReportQueryRepository reportQueryRepository, JdbcTemplate jdbcTemplate) {
        this.reportQueryRepository = reportQueryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


    //  관리자 신고 목록 조회
    public List<ReportPendingResponse> getReportsForAdmin(ReportStatus status) {

        List<ReportPendingResponse> result = new ArrayList<>();

        // 1) 게시글 신고 5회 이상인 게시글 목록
        List<Long> pendingThreadIds = reportQueryRepository.findThreadIdsByStatusReportedOverFive(status.name());

        for (Long threadId : pendingThreadIds) {

            List<ReportPendingResponse> rows = jdbcTemplate.query(
                    """
                    SELECT 
                       rep.report_id AS reportId,
                       u.user_id AS reportedUserId,
                       u.user_nickname AS targetNickname,
                       t.threads_id AS threadId,
                       t.report_count AS reportCount,
                       0 AS deleteCount,
                       t.created_at AS createdAt,
                       q.content AS quoteContent,
                       rep.status AS status,
                       rep.admin_comment AS adminComment
                    FROM report rep
                    JOIN threads t ON rep.threads_id = t.threads_id
                    JOIN users u ON t.user_id = u.user_id
                    JOIN quote q ON t.quote_id = q.quote_id
                    WHERE rep.status = ?
                      AND rep.threads_id = ?
                    ORDER BY rep.created_at DESC
                    LIMIT 1
                    """,

                    (rs, rowNum) -> new ReportPendingResponse(
                            rs.getLong("reportId"),
                            rs.getLong("reportedUserId"),
                            rs.getString("targetNickname"),
                            rs.getLong("threadId"),
                            rs.getInt("reportCount"),
                            rs.getInt("deleteCount"),
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getString("quoteContent"),
                            ReportStatus.valueOf(rs.getString("status")),
                            rs.getString("adminComment")
                    ),
                    status.name(),
                    threadId
            );

            if (!rows.isEmpty()) {result.add(rows.get(0));}
        }

        return result;
    }

    // 요약
    public ReportSummaryResponse getReportSummary() {

        long total = reportQueryRepository.count();
        long pending = reportQueryRepository.countByStatus(ReportStatus.PENDING);
        long completed = reportQueryRepository.countByStatusIn(
                List.of(ReportStatus.VALID, ReportStatus.REJECTED)
        );

        return new ReportSummaryResponse(total, pending, completed);
    }

    // 상세
    public ReportPendingResponse getReportDetail(Long reportId) {

        return jdbcTemplate.queryForObject(
                """
                SELECT
                    rep.report_id AS reportId,
                    u.user_id AS reportedUserId,
                    u.user_nickname AS targetNickname,
                    t.threads_id AS threadId,
                    t.report_count AS reportCount,
                    0 AS deleteCount,
                    t.created_at AS createdAt,
                    q.content AS quoteContent,
                    rep.status AS status,
                    rep.admin_comment AS adminComment
                    FROM report rep
                    LEFT JOIN threads t ON rep.threads_id = t.threads_id
                    LEFT JOIN users u ON u.user_id = t.user_id
                    LEFT JOIN quote q ON q.quote_id = t.quote_id
                    WHERE rep.report_id = ?
                    """,

                (rs, rowNum) -> new ReportPendingResponse(
                        rs.getLong("reportId"),
                        rs.getLong("reportedUserId"),
                        rs.getString("targetNickname"),
                        rs.getObject("threadId") == null ? null : rs.getLong("threadId"),
                        rs.getInt("reportCount"),
                        rs.getInt("deleteCount"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getString("quoteContent"),
                        ReportStatus.valueOf(rs.getString("status")),
                        rs.getString("adminComment")
                ),
                reportId
        );
    }
}
