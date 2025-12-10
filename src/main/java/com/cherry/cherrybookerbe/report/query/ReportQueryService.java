package com.cherry.cherrybookerbe.report.query;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityReply;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import com.cherry.cherrybookerbe.report.query.dto.ReportPendingResponse;
import com.cherry.cherrybookerbe.report.query.dto.ReportSummaryResponse;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
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

    public ReportQueryService(
            ReportQueryRepository reportQueryRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.reportQueryRepository = reportQueryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 관리자 신고 목록 조회. 5회 이상 + PENDING
    public List<ReportPendingResponse> getPendingReportsForAdmin() {

        List<ReportPendingResponse> result = new ArrayList<>();
        List<Long> pendingThreadIds =
                reportQueryRepository.findPendingThreadIdsReportedOverFive();
        for (Long threadId : pendingThreadIds) {
            ReportPendingResponse response = jdbcTemplate.queryForObject(
                    """
               
                   SELECT 
                   u.user_id AS reportedUserId,
                   u.user_nickname AS targetNickname,
                   t.threads_id AS threadId,
                   t.report_count AS reportCount,
                   t.created_at AS createdAt,
                   q.content AS quoteContent
               FROM threads t
               JOIN users u ON t.user_id = u.user_id
               JOIN quote q ON t.quote_id = q.quote_id
               WHERE t.threads_id = ?
               """,

                (rs, rowNum) -> new ReportPendingResponse(
                        rs.getLong("reportedUserId"),
                        rs.getString("targetNickname"),
                        rs.getLong("threadId"),
                        null, // 댓글 아님
                        rs.getInt("reportCount"),
                        0, // deleteCount = 사용 안 함 → 0으로 고정 가능
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getString("quoteContent"),
                        ReportStatus.PENDING,
                        null
                ),
            threadId
        );
        result.add(response);
        }
        // ✅ 2. 댓글 신고 목록
        List<Long> pendingReplyIds =
                reportQueryRepository.findPendingReplyIdsReportedOverFive();

        for (Long replyId : pendingReplyIds) {

            ReportPendingResponse response = jdbcTemplate.queryForObject(
                    """
                    SELECT 
                        u.user_id AS reportedUserId,
                        u.user_nickname AS targetNickname,
                        r.threads_reply_id AS threadReplyId,
                        r.report_count AS reportCount,
                        r.created_at AS createdAt,
                        q.content AS quoteContent
                    FROM threads_reply r
                    JOIN users u ON r.user_id = u.user_id
                    JOIN quote q ON r.quote_id = q.quote_id
                    WHERE r.threads_reply_id = ?
                    """,

                    (rs, rowNum) -> new ReportPendingResponse(
                            rs.getLong("reportedUserId"),
                            rs.getString("targetNickname"),
                            null, // ✅ 게시글 아님
                            rs.getLong("threadReplyId"),
                            rs.getInt("reportCount"),
                            0,
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getString("quoteContent"),
                            ReportStatus.PENDING,
                            null
                    ),
                    replyId
            );

            result.add(response);
        }
        return result;
    }

    public ReportSummaryResponse getReportSummary() {

        long total = reportQueryRepository.count();
        long pending = reportQueryRepository.countByStatus(ReportStatus.PENDING);
        long completed = reportQueryRepository.countByStatusIn(
            List.of(ReportStatus.VALID, ReportStatus.REJECTED)
        );
        return new ReportSummaryResponse(total, pending, completed);
    }

    public ReportPendingResponse getReportDetail(Long reportId) {

        return jdbcTemplate.queryForObject(
                """
                
                SELECT\s
                    u.user_id AS reportedUserId,
                    u.user_nickname AS targetNickname,
                    t.threads_id AS threadId,
                    rep.threads_reply_id AS threadReplyId,
                    COALESCE(t.report_count, tr.report_count) AS reportCount,
                    0 AS deleteCount,
                    COALESCE(t.created_at, tr.created_at) AS createdAt,
                    q.content AS quoteContent,
                    rep.status AS status,
                    rep.admin_comment AS adminComment
                FROM report rep
                LEFT JOIN threads t ON rep.threads_id = t.threads_id
                LEFT JOIN threads_reply tr ON rep.threads_reply_id = tr.threads_reply_id
                JOIN users u ON u.user_id = COALESCE(t.user_id, tr.user_id)
                JOIN quote q ON q.quote_id = COALESCE(t.quote_id, tr.quote_id)
                WHERE rep.report_id = ?
                """,

                (rs, rowNum) -> new ReportPendingResponse(
                        rs.getLong("reportedUserId"),
                        rs.getString("targetNickname"),
                        rs.getObject("threadId") == null ? null : rs.getLong("threadId"),
                        rs.getObject("threadReplyId") == null ? null : rs.getLong("threadReplyId"),
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

