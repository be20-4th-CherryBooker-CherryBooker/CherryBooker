package com.cherry.cherrybookerbe.user.command.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.user.command.dto.request.UpdateNicknameRequest;
import com.cherry.cherrybookerbe.user.command.service.UserCommandServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserCommandController {

    private final UserCommandServiceImpl userCommandService;

    // 닉네임 변경
    @PatchMapping("/users/user")
    public ResponseEntity<ApiResponse<Void>> updateUserNickName(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        userCommandService.updateNickname(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 탈퇴(소프트 삭제)
    @DeleteMapping("/users/user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userCommandService.deleteUser(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
