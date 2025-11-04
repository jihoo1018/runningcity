package com.runningcity.user.service;

import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.user.dto.NicknameUpdateRequest;
import com.runningcity.user.dto.NicknameUpdateResponse;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 닉네임 수정
     * @param userId 사용자 ID
     * @param request 닉네임 수정 요청
     * @return 수정된 사용자 정보
     */
    @Transactional
    public NicknameUpdateResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));

        // 2. 닉네임 중복 체크 (본인 제외)
        if (userRepository.existsByNicknameAndUserIdNot(request.getNickname(), userId)) {
            throw new BaseException(CommonResponseCode.NICKNAME_DUPLICATE);
        }

        // 3. 닉네임 수정 (빌더 패턴 사용)
        User updatedUser = User.builder()
                .userId(user.getUserId())
                .googleId(user.getGoogleId())
                .email(user.getEmail())
                .nickname(request.getNickname()) // 수정
                .profileImageUrl(user.getProfileImageUrl())
                .hasCompletedOnboarding(user.getHasCompletedOnboarding())
                .level(user.getLevel())
                .totalRunningEnergy(user.getTotalRunningEnergy())
                .isActive(user.getIsActive())
                .build();

        // 4. 저장
        userRepository.save(updatedUser);

        // 5. 응답 생성
        return NicknameUpdateResponse.builder()
                .userId(updatedUser.getUserId())
                .nickname(updatedUser.getNickname())
                .build();
    }
}

