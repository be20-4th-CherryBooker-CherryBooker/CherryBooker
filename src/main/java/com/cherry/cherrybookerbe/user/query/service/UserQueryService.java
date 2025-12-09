package com.cherry.cherrybookerbe.user.query.service;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MypageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    // 내 정보 조회
    public MypageResponse getMypageInfo(Integer userId){
        User user =  userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다." + userId));
        return MypageResponse.from(user);
    }

}
