package com.cherry.cherrybookerbe.report.command;

import com.cherry.cherrybookerbe.report.domain.Report;

// 신고 저장
// 스레드id 기준 신고 개수 count
public interface ReportCommandRepository
        extends JpaRepository<Report, Long> {
}
