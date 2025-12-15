package com.cherry.cherrybookerbe.user.command.service;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserStatus;
import com.cherry.cherrybookerbe.user.command.dto.request.UpdateNicknameRequest;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public void updateNickname(Integer userId , UpdateNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        if(user.getUserStatus() == UserStatus.DELETE){
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        user.updateNickName(request.getNickname());
    }

    @Transactional
    @Override
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        if (user.getUserStatus() == UserStatus.DELETE) {
            throw new IllegalArgumentException("이미 탈퇴 처리된 회원입니다.");
        }

        user.softDelete();
    }
}
