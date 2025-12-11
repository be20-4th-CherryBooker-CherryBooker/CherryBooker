package com.cherry.cherrybookerbe.report.command;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityReply;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.report.command.dto.CreateReportRequest;
import com.cherry.cherrybookerbe.report.command.dto.ProcessReportRequest;
import com.cherry.cherrybookerbe.report.domain.Report;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import com.cherry.cherrybookerbe.report.query.ReportQueryRepository;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 신고 저장
// 5회 이상 => 쿼리 서비스 부르기
// 관리자 reject 처리 시 → thread에서 삭제 처리
// 삭제되면 → user에서 정리 처리
@Service
@Transactional
public class ReportCommandService {

    private final ReportCommandRepository reportCommandRepository;
    private final ReportQueryRepository reportQueryRepository;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    public ReportCommandService(
            ReportCommandRepository reportCommandRepository,
            ReportQueryRepository reportQueryRepository,
            JdbcTemplate jdbcTemplate)
    {
        this.reportCommandRepository = reportCommandRepository;
        this.reportQueryRepository = reportQueryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 신고자 조회
    private User findUserById(Long userId) {
        String sql = "select user_id from users where user_id = ?";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, userId);

        if(count == null || count ==0){ // 신고자 존재 여부 확인
            throw new IllegalArgumentException("User not found");
        }
        return em.getReference(User.class, userId); //report에 넣을 id만 있는 jpa 용 임시객체
    }
    // 게시글 조회
    private CommunityThread findCommunityThreadById(Long threadId) {
        String sql = "select threads_id from threads where threads_id = ? AND is_deleted = false";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, threadId);

        if(count ==null || count==0) {
            throw new IllegalArgumentException("Community thread not found");
        }
        return em.getReference(CommunityThread.class, threadId);
    }
    // 댓글 조회
    private CommunityReply findCommunityReplyById(Long replyId) {
        String sql = "select threads_reply_id from threads_reply where threads_reply_id = ? AND is_deleted = false";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, replyId);

        if(count ==null || count ==0) {
            throw new IllegalArgumentException("Community reply not found");
        }
        return em.getReference(CommunityReply.class, replyId);
    }


    // 신고 등록
    public void create(CreateReportRequest createRequest) {
        Long reporterId =  createRequest.getReporterId();
        Long threadId =  createRequest.getThreadId();
        Long threadReplyId = createRequest.getThreadsReplyId();

        //신고 검증
        if (threadId == null && threadReplyId == null) {
            throw new IllegalArgumentException("신고 대상이 존재하지 않습니다.");
        }

        // 동시 신고 차단
        if (threadId != null && threadReplyId != null) {
            throw new IllegalArgumentException("게시글과 댓글은 동시에 신고할 수 없습니다.");
        }

        // 신고자 직접 조회
        User reporter = findUserById(reporterId);
        CommunityThread reportedThread = null;
        CommunityReply reportedReply = null;

        if(threadId !=null) {
            reportedThread = findCommunityThreadById(threadId);
        }
        if(threadReplyId != null) {
            reportedReply = findCommunityReplyById(threadReplyId);
        }

        // 게시글 or 댓글 신고 등록
        Report report = Report.builder()
                    .user(reporter) //신고자
                    .threads(reportedThread) //신고 대상 게시글인 경우
                    .threadsReply(reportedReply) // 댓글 신고인 경우
                    .status(ReportStatus.PENDING) // 게시글
                    .build();
        reportCommandRepository.save(report); // 저장

    }
    // 관리자 판단
    public void process(ProcessReportRequest processRequest)  {
        Long reportId = processRequest.getReportId(); // 관리자가 클릭한 신고 1건
        ReportStatus status = processRequest.getStatus(); // valid or rejected
        String adminComment = processRequest.getAdminComment();

        Report report = reportCommandRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고가 존재하지 않습니다."));

        //신고 받은 게시글 상태 변경(삭제 or 반려)

        if(status == ReportStatus.VALID) {
            report.approve(adminComment); // 삭제
        } else if(status == ReportStatus.REJECTED) {
            report.reject(adminComment); // 반려
        }else {
            throw new IllegalArgumentException("잘못된 상태 입니다.");
        }

        // 한번더 막는 장치 (아래의 로직 실행 전에)
        if(status != ReportStatus.VALID) return;

        // 게시글 삭제
        if(report.getThreads() != null) {
            Integer threadId = report.getThreads().getId();
            Integer reportedUserId = report.getThreads().getUserId();

            jdbcTemplate.update(
                    "UPDATE threads SET is_deleted = 1 WHERE threads_id=?",
                    threadId
            );
            // 2) delete_count 증가
            jdbcTemplate.update(
                    "UPDATE users SET delete_count = delete_count + 1 WHERE user_id = ?",
                    reportedUserId
            );

            // 3) delete_count가 3 이상이면 정지 처리
            jdbcTemplate.update(
                    "UPDATE users SET user_status = 'SUSPENDED' WHERE user_id = ? AND delete_count >= 3",
                    reportedUserId
            );

        }

        // 댓글 삭제
        if(report.getThreadsReply() != null) {
            Integer replyId = report.getThreadsReply().getId();
            Integer reportedUserId = report.getThreadsReply().getUserId();

            jdbcTemplate.update(
                    "UPDATE threads_reply SET is_deleted = 1, deleted_at = NOW() WHERE threads_reply_id = ?",
                    replyId
            );
            // 2) delete_count 증가
            jdbcTemplate.update(
                    "UPDATE users SET delete_count = delete_count + 1 WHERE user_id = ?",
                    reportedUserId
            );

            // 3) delete_count가 3 이상이면 정지 처리
            jdbcTemplate.update(
                    "UPDATE users SET user_status = 'SUSPENDED' WHERE user_id = ? AND delete_count >= 3",
                    reportedUserId
            );
        }

    }
}
