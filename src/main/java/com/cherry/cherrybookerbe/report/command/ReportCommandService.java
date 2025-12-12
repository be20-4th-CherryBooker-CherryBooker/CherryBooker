package com.cherry.cherrybookerbe.report.command;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.report.command.dto.CreateReportRequest;
import com.cherry.cherrybookerbe.report.command.dto.ProcessReportRequest;
import com.cherry.cherrybookerbe.report.domain.Report;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
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
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    public ReportCommandService(
            ReportCommandRepository reportCommandRepository,
            JdbcTemplate jdbcTemplate)
    {
        this.reportCommandRepository = reportCommandRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 신고자 조회
    private User findUserById(Long userId) {

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from users where user_id = ?",
                Integer.class,
                userId
        );


        if(count == null || count ==0){ // 신고자 존재 여부 확인
            throw new IllegalArgumentException("User not found");
        }
        return em.getReference(User.class, userId); //report에 넣을 id만 있는 jpa 용 임시객체
    }
    // 게시글 조회
    private CommunityThread findCommunityThreadById(Long threadId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from threads where threads_id = ? AND is_deleted = 0",
                Integer.class,
                threadId
        );
        if(count ==null || count==0) {
            throw new IllegalArgumentException("Community thread not found");
        }
        return em.getReference(CommunityThread.class, threadId);
    }

    // 신고 등록
    public void create(CreateReportRequest createRequest) {
        Long reporterId =  createRequest.getReporterId(); // TODO: 타입 맞는지 확인 필요
        Long threadId =  createRequest.getThreadId(); // TODO: 타입 맞는지 확인 필요

        //신고 검증
        if (threadId == null ) {
            throw new IllegalArgumentException("신고 대상이 존재하지 않습니다.");
        }

        // 신고자 직접 조회
        User reporter = findUserById(reporterId);
        CommunityThread reportedThread = null;

        if(threadId !=null) {
            reportedThread = findCommunityThreadById(threadId);
        }

        // 게시글  신고 등록
        Report report = Report.builder()
                    .user(reporter) //신고자
                    .threads(reportedThread) //신고 대상 게시글인 경우
                    .status(ReportStatus.PENDING) // 게시글
                    .build();
        reportCommandRepository.save(report); // 저장

        //신고 접수 시 누적 신고 횟수(reported_count) 증가
        jdbcTemplate.update(
                    "UPDATE threads SET report_count = report_count + 1 WHERE threads_id = ?",
                    threadId
        );


    }
    // 관리자 판단; valid -> 삭제, 제제, rejected -> report_count +1이다.
    public void process(ProcessReportRequest processRequest)  {
        Long reportId = processRequest.getReportId(); // 관리자가 클릭한 신고 1건
        ReportStatus status = processRequest.getStatus(); // valid or rejected
        String adminComment = processRequest.getAdminComment();

        // 신고 조회
        Report report = reportCommandRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고가 존재하지 않습니다."));

        // 이미 처리된 신고 방지
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        CommunityThread reportedThread = report.getThreads();
        if (reportedThread == null) {
            throw new IllegalStateException("신고 대상 게시글이 없습니다.");
        }
        Integer threadId = reportedThread.getId();

        jdbcTemplate.update(
                """
                UPDATE report
                SET status = ?,
                    admin_comment = ?
                WHERE threads_id = ?
                  AND status = 'PENDING'
                """,
                status.name(),
                adminComment,
                threadId
        );

        //신고 받은 게시글 상태 변경(삭제 or 반려)

        if(status == ReportStatus.VALID) {
            Integer reportedUserId = reportedThread.getUserId();


                // 삭제
            jdbcTemplate.update(
                        "UPDATE threads SET is_deleted = 1 WHERE threads_id=?",
                        threadId
            );
                // 삭제 delete_count 증가
            jdbcTemplate.update(
                        "UPDATE users SET delete_count = delete_count + 1 WHERE user_id = ?",
                        reportedUserId
            );
                // delete_count가 3 이상이면 정지 처리
            jdbcTemplate.update(
                        "UPDATE users SET user_status = 'SUSPENDED' WHERE user_id = ? AND delete_count >= 3",
                        reportedUserId
            );

            return;

        }

        // 반려 처리
        if(status == ReportStatus.REJECTED){
            return;
        }
        throw new IllegalArgumentException("잘못된 상태입니다.");

    }
}
