package com.runningcity.auth.service;

import com.runningcity.auth.dto.CheckEmailResponse;
import com.runningcity.auth.dto.LoginRequest;
import com.runningcity.auth.dto.LoginSuccessData;
import com.runningcity.auth.dto.SignupRequest;
import com.runningcity.auth.exception.AuthResponseCode;
import com.runningcity.auth.repository.UserAuthRepository;
import com.runningcity.global.exception.BaseException;
import com.runningcity.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCodeGenerator userCodeGenerator;

    @Transactional(readOnly = true)
    public CheckEmailResponse checkEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        return new CheckEmailResponse(exists);
    }

    @Transactional
    public void signup(SignupRequest req) {
        // 1) 비밀번호 확인
        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new BaseException(AuthResponseCode.PASSWORD_MISMATCH);
        }

        // 2) 이메일 중복
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BaseException(AuthResponseCode.EMAIL_ALREADY_EXISTS);
        }

        // 3) 비밀번호 인코딩
        String encoded;
        try {
            encoded = passwordEncoder.encode(req.getPassword());
        } catch (RuntimeException e) {
            throw new BaseException(AuthResponseCode.ENCODING_FAILED);
        }

        // 4) user_code 생성 (충돌 방지 재시도)
        String userCode = null;
        int maxRetry = 5;
        for (int i = 0; i < maxRetry; i++) {
            String candidate = userCodeGenerator.generate(10); // 10자리
            if (!userRepository.existsByUserCode(candidate)) {
                userCode = candidate;
                break;
            }
        }
        if (userCode == null) {
            userCode = userCodeGenerator.generate(12);
        }

        // 5) 저장
        User user = User.builder()
                .email(req.getEmail())
                .password(encoded)     // 엔티티 컬럼명이 password
                .userCode(userCode)
                .isActive(true)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // user_code UNIQUE 극희박 충돌 등
            throw new BaseException(AuthResponseCode.INTERNAL_ERROR);
        }
    }


    @Transactional(readOnly = true)
    public LoginSuccessData login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BaseException(AuthResponseCode.EMAIL_NOT_FOUND));

        if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BaseException(AuthResponseCode.PASSWORD_MISMATCH);
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BaseException(AuthResponseCode.INACTIVE_USER);
        }

        return LoginSuccessData.builder()
                .userId(user.getUserId())
                .userNickname(user.getNickname())
                .userCode(user.getUserCode())
                .totalexp(user.getTotalExp())
                .build();
    }
}
