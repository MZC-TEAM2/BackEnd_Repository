package com.mzc.backend.lms.domains.user.auth.usecase.impl;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.auth.usecase.CheckEmailUseCase;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * 이메일 확인 유스케이스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckEmailUseCaseImpl implements CheckEmailUseCase {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(String email) {
        if (!isValidFormat(email)) {
            return false;
        }

        String encryptedEmail = encryptionService.encryptEmail(email);
        boolean available = !userRepository.existsByEmail(encryptedEmail);

        log.debug("이메일 중복 확인: email={}, available={}", email, available);

        return available;
    }

    @Override
    public boolean isValidFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

}