package com.cherry.cherrybookerbe.report.query;

import com.cherry.cherrybookerbe.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
// 5회 이상 신고 받은 글 목록(pending상태) 조회
public interface ReportQueryRepository extends JpaRepository<Report, Long> {
}
