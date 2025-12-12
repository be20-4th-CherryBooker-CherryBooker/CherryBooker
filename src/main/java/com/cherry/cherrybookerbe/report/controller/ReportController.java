package com.cherry.cherrybookerbe.report.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.report.command.ReportCommandService;
import com.cherry.cherrybookerbe.report.command.dto.ProcessReportRequest;
import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import com.cherry.cherrybookerbe.report.query.ReportQueryService;
import com.cherry.cherrybookerbe.report.query.dto.ReportPendingResponse;
import com.cherry.cherrybookerbe.report.query.dto.ReportSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final ReportCommandService reportCommandService;

    // 신고 통계 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary")
    public ApiResponse<ReportSummaryResponse> getReportSummary() {
        return ApiResponse.success(
                reportQueryService.getReportSummary()
        );
    }

    // 신고 목록 조회 (5회 이상 신고 받은 게시물
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<ReportPendingResponse>> getReportsForAdmin(
            @RequestParam(defaultValue = "PENDING") ReportStatus status
    ) {
        return ApiResponse.success(
                reportQueryService.getReportsForAdmin(status)
        );
    }

    //  신고 상세 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{reportId}")
    public ApiResponse<ReportPendingResponse> getReportDetail(@PathVariable Long reportId) {
        return ApiResponse.success(
                reportQueryService.getReportDetail(reportId)
        );
    }

    //  신고 처리 (VALID / REJECTED)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/process")
    public ApiResponse<Void> processReport(@RequestBody ProcessReportRequest request) {
        reportCommandService.process(request);
        return ApiResponse.success(null);
    }
}

