package com.cherry.cherrybookerbe.user.query.command;

import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MypageResponse;
import com.cherry.cherrybookerbe.user.query.dto.reponse.UserListResponse;
import com.cherry.cherrybookerbe.user.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/user")
public class UserQueryController {

    private final UserQueryService userQueryService;


    // Todo (관리자)전체 유저 조회


    //마이페이지 조회
    @GetMapping("/me")
    public ResponseEntity<MypageResponse> getMypageInfo(@AuthenticationPrincipal UserPrincipal principal) {

        MypageResponse myPageInfo = userQueryService.getMypageInfo(principal.userId());

        return ResponseEntity.ok(myPageInfo);
    }

}
