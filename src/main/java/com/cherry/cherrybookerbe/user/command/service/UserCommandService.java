package com.cherry.cherrybookerbe.user.command.service;

import com.cherry.cherrybookerbe.user.command.dto.request.UpdateNicknameRequest;

public interface UserCommandService {
    void updateNickname(Integer userId , UpdateNicknameRequest request);

    void deleteUser (Integer userId);
}
