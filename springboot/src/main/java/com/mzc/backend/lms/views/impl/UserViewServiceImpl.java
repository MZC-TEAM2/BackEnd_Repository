package com.mzc.backend.lms.views.impl;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 정보 공통 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserViewServiceImpl implements UserViewService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPrimaryContactRepository userPrimaryContactRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional(readOnly = true)
    public String getUserType(String userId) {
        log.debug("Getting user type for userId: {}", userId);

        try {
            Long id = Long.parseLong(userId);

            // 학생인지 확인
            if (studentRepository.findById(id).isPresent()) {
                return "STUDENT";
            }

            // 교수인지 확인
            if (professorRepository.findById(id).isPresent()) {
                return "PROFESSOR";
            }
        } catch (NumberFormatException e) {
            log.debug("Invalid user id format: {}", userId);
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserName(String userId) {
        log.debug("Getting user name for userId: {}", userId);

        try {
            Long id = Long.parseLong(userId);
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(id);

            if (profileOpt.isEmpty()) {
                return null;
            }

            String encryptedName = profileOpt.get().getName();
            return decryptName(encryptedName);
        } catch (NumberFormatException e) {
            log.debug("Invalid user id format: {}", userId);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getUserNames(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Getting names for {} users", userIds.size());

        // String을 Long으로 변환
        List<Long> longIds = userIds.stream()
            .map(id -> {
                try {
                    return Long.parseLong(id);
                } catch (NumberFormatException e) {
                    log.debug("Invalid user id format: {}", id);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (longIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 프로젝션 쿼리로 userId와 name만 조회
        List<Object[]> results = userProfileRepository.findNamesByUserIds(longIds);

        Map<String, String> nameMap = new HashMap<>();
        for (Object[] result : results) {
            Long longUserId = (Long) result[0];
            String encryptedName = (String) result[1];
            String decryptedName = decryptName(encryptedName);
            nameMap.put(longUserId.toString(), decryptedName);
        }

        return nameMap;
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserProfileImageUrl(String userId) {
        log.debug("Getting profile image URL for userId: {}", userId);

        try {
            Long id = Long.parseLong(userId);
            Optional<UserProfileImage> imageOpt = userProfileImageRepository
                .findByUserId(id);

            return imageOpt.map(UserProfileImage::getImageUrl).orElse(null);
        } catch (NumberFormatException e) {
            log.debug("Invalid user id format: {}", userId);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        try {
            Long id = Long.parseLong(userId);
            return userRepository.existsById(id);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 이름 복호화
     */
    private String decryptName(String encryptedName) {
        if (encryptedName == null) {
            return null;
        }

        try {
            return encryptionService.decryptPersonalInfo(encryptedName);
        } catch (Exception e) {
            log.error("Failed to decrypt name", e);
            return encryptedName;
        }
    }
}