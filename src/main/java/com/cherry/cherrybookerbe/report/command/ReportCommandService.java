package com.cherry.cherrybookerbe.report.command;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityReply;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.report.command.dto.CreateReportRequest;
import com.cherry.cherrybookerbe.report.command.dto.ProcessReportRequest;
import com.cherry.cherrybookerbe.report.domain.Report;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import com.cherry.cherrybookerbe.report.query.ReportQueryRepository;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
// 신고 저장
// 5회 이상 => 쿼리 서비스 부르기
// 관리자 reject 처리 시 → thread에서 삭제 처리
// 삭제되면 → user에서 정리 처리
public class ReportCommandService {

    private final ReportCommandRepository reportCommandRepository;
    private final ReportQueryRepository reportQueryRepository;
    /*
    *  private final UserRepository userRepository;
    private final CommunityThreadRepository threadRepository;
    private final CommunityReplyRepository replyRepository;
    private final ThreadService threadService;
    private final UserService userService;*/


    public ReportCommandService(
            ReportCommandRepository reportCommandRepository,
            ReportQueryRepository reportQueryRepository)
    {
        this.reportCommandRepository = reportCommandRepository;
        this.reportQueryRepository = reportQueryRepository;
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
        //  user, thread, threadReply repository가 없으므로 임시 더미 객체 (컴파일용)
        //레포지토리 생기면 삭제 예정
        User reporter = null;
        CommunityThread reportedThread = null;
        CommunityReply reportedReply = null;

        // user, thread 리포지토리 미생성에 따른 주석 처리
        /*신고자 조회
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자 유저 없음"));
        CommunityThread reportedThread = null;
        CommunityReply reportedReply = null;


        if(threadId != null) {
            /* 신고 게시글 레포지토리
            reportedThread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("신고 대상 게시글 없음"));
        if (threadReplyId != null) {
            reportedReply = replyRepository.findById(threadReplyId)
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상 댓글 없음"));
        }  */

        // 게시글 신고
        if (threadId != null) {
            Report report = Report.builder()
                    .user(reporter) //신고자
                    .threads(reportedThread) //신고 대상 게시글
                    .threadsReply(null)
                    .status(ReportStatus.PENDING) // 게시글
                    .build();
            reportCommandRepository.save(report);
        }
        // 댓글 신고
        if(threadReplyId != null) {
            Report report = Report.builder()
                    .user(reporter)
                    .threads(null)
                    .threadsReply(reportedReply) // 신고 대상 댓글
                    .status(ReportStatus.PENDING)
                    .build();
            reportCommandRepository.save(report);
        }

    }
    // 관리자 판단
    public void process(ProcessReportRequest processRequest) throws IllegalAccessException {
        Long reportId = processRequest.getReportId(); // 관리자가 클릭한 신고 1건
        ReportStatus status = processRequest.getStatus(); // valid or rejected

        Report report = reportCommandRepository.findById(reportId)
                .orElseThrow(() -> new IllegalAccessException("신고가 존재하지 않습니다."));

        //신고 받은 게시글 상태 변경(삭제 or 반려)
        if(status == ReportStatus.VALID) {
            report.approve(); // 삭제
        } else if(status == ReportStatus.REJECTED) {
            report.reject(); // 반려
        }else {
            throw new IllegalAccessException("잘못된 상태 입니다.");
        }

        //valid -> soft delete
        if (status == ReportStatus.VALID) {

            // 게시물 신고
            if(report.getThreads() != null) {
                CommunityThread reportedThreads = report.getThreads();

                /*
                Long reportedUserId = targetThread.getUserId();
                User reportedUser = userRepository.findById(reportedUserId)
                        .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
                /*
                threadService.delete(reportedThread);
                userService.increaseDeleteCount(reportedUser);
                userService.suspendIfOverLimit(reportedUser);
                */
            }
            //댓글 신고
            if(report.getThreadsReply() != null) {
                CommunityReply reportedReply = report.getThreadsReply();

                /*
                Long reportedUserId = reportedReply.getUserId();
                User reportedUser = userRepository.findById(reportedUserId) // ✅ 제재 대상 유저
                        .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
                // 팀원 서비스 연결 전까지는 주석 유지
                /*
                threadService.delete(reportedReply);
                userService.increaseDeleteCount(reportedUserId);
                userService.suspendIfOverLimit(reportedUserId);
             */
            }

        }

    }
}
