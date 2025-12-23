package com.mzc.backend.lms.views.impl;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.views.ProfessorViewService;
import com.mzc.backend.lms.views.user.ProfessorView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 교수 정보 통합 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorViewServiceImpl implements ProfessorViewService {
	
	private final ProfessorRepository professorRepository;
	private final ProfessorDepartmentRepository professorDepartmentRepository;
	private final UserProfileRepository userProfileRepository;
	private final UserPrimaryContactRepository userPrimaryContactRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final DepartmentRepository departmentRepository;
	private final EncryptionService encryptionService;
	
	@Override
	@Transactional(readOnly = true)
	public ProfessorView getProfessor(String professorNumber) {
		return findProfessor(professorNumber)
				.orElseThrow(() -> UserException.professorNotFound(professorNumber));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<ProfessorView> findProfessor(String professorNumber) {
		log.debug("Finding professor by professorNumber: {}", professorNumber);
		
		try {
			Long professorId = Long.parseLong(professorNumber);
			// Native Query로 전체 정보 조회
			Object[] result = professorRepository.findProfessorFullInfoById(professorId);
			
			if (result == null) {
				return Optional.empty();
			}
			
			return Optional.of(mapToProfessorView(result));
		} catch (NumberFormatException e) {
			log.debug("Invalid professor number format: {}", professorNumber);
			return Optional.empty();
		}
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, ProfessorView> getProfessorsByNumbers(List<String> professorNumbers) {
		if (professorNumbers == null || professorNumbers.isEmpty()) {
			return Collections.emptyMap();
		}
		
		log.debug("Finding {} professors by numbers", professorNumbers.size());
		
		// String을 Long으로 변환
		List<Long> professorIds = professorNumbers.stream()
				.map(num -> {
					try {
						return Long.parseLong(num);
					} catch (NumberFormatException e) {
						log.debug("Invalid professor number format: {}", num);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		if (professorIds.isEmpty()) {
			return Collections.emptyMap();
		}
		
		// Native Query로 여러 교수 정보 조회
		List<Object[]> results = professorRepository.findProfessorsFullInfoByIds(professorIds);
		
		return results.stream()
				.map(this::mapToProfessorView)
				.collect(Collectors.toMap(
						ProfessorView::getProfessorNumber,
						professor -> professor
				));
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public boolean existsByProfessorNumber(String professorNumber) {
		try {
			Long professorId = Long.parseLong(professorNumber);
			return professorRepository.existsById(professorId);
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Native Query 결과를 ProfessorView로 매핑
	 * <p>
	 * 컬럼 순서:
	 * 0: user_id, 1: professor_number, 2: appointment_date,
	 * 3: email, 4: profile_name, 5: phone_number, 6: office_number,
	 * 7: department_id, 8: department_name,
	 * 9: college_id, 10: college_name,
	 * 11: dept_join_date, 12: dept_active,
	 * 13: profile_image_url
	 */
	private ProfessorView mapToProfessorView(Object[] result) {
		return ProfessorView.builder()
				.userId(((Number) result[0]).longValue())
				.professorNumber((String) result[1])
				.appointmentDate(result[2] != null ? ((java.sql.Date) result[2]).toLocalDate() : null)
				.email(result[3] != null ? decryptEmail((String) result[3]) : null)
				.name(result[4] != null ? decryptName((String) result[4]) : null)
				.phoneNumber(result[5] != null ? decryptPhoneNumber((String) result[5]) : null)
				.officeNumber((String) result[6])
				.departmentId(result[7] != null ? ((Number) result[7]).longValue() : null)
				.departmentName((String) result[8])
				.collegeId(result[9] != null ? ((Number) result[9]).longValue() : null)
				.collegeName((String) result[10])
				.departmentJoinDate(result[11] != null ? ((java.sql.Date) result[11]).toLocalDate() : null)
				.isDepartmentActive(result[12] != null ? (Boolean) result[12] : false)
				.profileImageUrl((String) result[13])
				.position(null) // 필요시 추가
				.officeLocation(null) // 필요시 추가
				.createdAt(null) // 필요시 추가
				.build();
	}
	
	/**
	 * 이메일 복호화
	 */
	private String decryptEmail(String encryptedEmail) {
		try {
			return encryptionService.decryptEmail(encryptedEmail);
		} catch (Exception e) {
			log.error("Failed to decrypt email", e);
			return encryptedEmail;
		}
	}
	
	/**
	 * 이름 복호화
	 */
	private String decryptName(String encryptedName) {
		try {
			return encryptionService.decryptPersonalInfo(encryptedName);
		} catch (Exception e) {
			log.error("Failed to decrypt name", e);
			return encryptedName;
		}
	}
	
	/**
	 * 전화번호 복호화
	 */
	private String decryptPhoneNumber(String encryptedPhoneNumber) {
		try {
			return encryptionService.decryptPhoneNumber(encryptedPhoneNumber);
		} catch (Exception e) {
			log.error("Failed to decrypt phone number", e);
			return encryptedPhoneNumber;
		}
	}
}
