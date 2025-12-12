package com.cherry.cherrybookerbe.user.query.command;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MyPageResponse;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MyProfileResponse;
import com.cherry.cherrybookerbe.user.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/user")
public class UserQueryController {

    private final UserQueryService userQueryService;

    //내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        log.info("내 프로필 조회 API 호출 - userId: {}", principal.userId());
        MyProfileResponse myPageInfo = userQueryService.getMyProfile(principal.userId());

        return ResponseEntity.ok(ApiResponse.success(myPageInfo));
    }

    // 마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPageStatistics(@AuthenticationPrincipal UserPrincipal principal) {
        log.info("마이페이지 통계 조회 API 호출 - userId: {}", principal.userId());
        MyPageResponse response = userQueryService.getMyPageInfo(principal.userId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
