package com.cherry.cherrybookerbe.user.command.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateNicknameRequest {

    @NotBlank(message = "새로운 닉네임을 입력해주세요.")
    String nickkname;
}
