package com.mzc.backend.lms.domains.user.profile.service;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;
import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.exceptions.UserErrorCode;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 프로필 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
	
	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final UserPrimaryContactRepository userPrimaryContactRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final StudentRepository studentRepository;
	private final StudentDepartmentRepository studentDepartmentRepository;
	private final ProfessorRepository professorRepository;
	private final ProfessorDepartmentRepository professorDepartmentRepository;
	private final EncryptionService encryptionService;
	
	@Override
	public ProfileResponseDto getMyProfile(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		
		ProfileResponseDto.ProfileResponseDtoBuilder builder = ProfileResponseDto.builder()
				.userId(userId)
				.email(encryptionService.decryptEmail(user.getEmail()));
		
		// 프로필 정보 조회
		userProfileRepository.findByUserId(userId)
				.ifPresent(profile -> builder.name(encryptionService.decryptName(profile.getName())));
		
		// 연락처 정보 조회
		userPrimaryContactRepository.findByUserId(userId)
				.ifPresent(contact -> {
					builder.mobileNumber(decryptIfNotNull(contact.getMobileNumber()))
							.homeNumber(decryptIfNotNull(contact.getHomeNumber()))
							.officeNumber(decryptIfNotNull(contact.getOfficeNumber()))
							.mobileVerified(contact.getMobileVerified());
				});
		
		// 프로필 이미지 조회
		userProfileImageRepository.findByUserId(userId)
				.ifPresent(image -> builder
						.profileImageUrl(image.getImageUrl())
						.thumbnailUrl(image.getThumbnailUrl()));
		
		// 사용자 타입별 정보 조회
		populateUserTypeInfo(userId, builder);
		
		return builder.build();
	}
	
	@Override
	@Transactional
	public ProfileResponseDto updateProfile(Long userId, ProfileUpdateRequestDto request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		
		// 이름 업데이트
		if (request.getName() != null && !request.getName().isEmpty()) {
			updateName(user, request.getName());
		}
		
		// 연락처 업데이트
		if (hasContactInfo(request)) {
			updateContact(user, request);
		}
		
		// 수정된 프로필 조회 후 반환
		return getMyProfile(userId);
	}
	
	private void updateName(User user, String name) {
		String encryptedName = encryptionService.encryptName(name);
		
		Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
		if (profileOpt.isPresent()) {
			UserProfile profile = profileOpt.get();
			profile.changeName(encryptedName);
			userProfileRepository.save(profile);
		} else {
			UserProfile newProfile = UserProfile.create(user, encryptedName);
			userProfileRepository.save(newProfile);
		}
	}
	
	private void updateContact(User user, ProfileUpdateRequestDto request) {
		Optional<UserPrimaryContact> contactOpt = userPrimaryContactRepository.findByUserId(user.getId());
		
		if (contactOpt.isPresent()) {
			UserPrimaryContact contact = contactOpt.get();
			updateContactFields(contact, request);
			userPrimaryContactRepository.save(contact);
		} else {
			String encryptedMobile = encryptIfNotNull(request.getMobileNumber());
			UserPrimaryContact newContact = UserPrimaryContact.builder()
					.user(user)
					.mobileNumber(encryptedMobile)
					.homeNumber(encryptIfNotNull(request.getHomeNumber()))
					.officeNumber(encryptIfNotNull(request.getOfficeNumber()))
					.build();
			userPrimaryContactRepository.save(newContact);
		}
	}
	
	private void updateContactFields(UserPrimaryContact contact, ProfileUpdateRequestDto request) {
		if (request.getMobileNumber() != null) {
			contact.updateMobileNumber(encryptionService.encryptPhoneNumber(request.getMobileNumber()));
		}
		if (request.getHomeNumber() != null) {
			contact.updateHomeNumber(encryptionService.encryptPhoneNumber(request.getHomeNumber()));
		}
		if (request.getOfficeNumber() != null) {
			contact.updateOfficeNumber(encryptionService.encryptPhoneNumber(request.getOfficeNumber()));
		}
	}
	
	private boolean hasContactInfo(ProfileUpdateRequestDto request) {
		return request.getMobileNumber() != null ||
				request.getHomeNumber() != null ||
				request.getOfficeNumber() != null;
	}
	
	private String encryptIfNotNull(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		return encryptionService.encryptPhoneNumber(value);
	}
	
	private void populateUserTypeInfo(Long userId, ProfileResponseDto.ProfileResponseDtoBuilder builder) {
		// 학생 정보 조회 시도
		Optional<Student> studentOpt = studentRepository.findById(userId);
		if (studentOpt.isPresent()) {
			Student student = studentOpt.get();
			builder.userType("STUDENT")
					.studentId(student.getStudentId())
					.admissionYear(student.getAdmissionYear())
					.grade(student.getGrade());
			
			// 학과 정보 조회
			studentDepartmentRepository.findByStudentId(userId)
					.ifPresent(sd -> {
						builder.departmentName(sd.getDepartment().getDepartmentName());
						if (sd.getDepartment().getCollege() != null) {
							builder.collegeName(sd.getDepartment().getCollege().getCollegeName());
						}
					});
			return;
		}
		
		// 교수 정보 조회 시도
		Optional<Professor> professorOpt = professorRepository.findById(userId);
		if (professorOpt.isPresent()) {
			Professor professor = professorOpt.get();
			builder.userType("PROFESSOR")
					.professorId(professor.getProfessorId())
					.appointmentDate(professor.getAppointmentDate());
			
			// 학과 정보 조회
			professorDepartmentRepository.findByProfessorId(userId)
					.ifPresent(pd -> {
						builder.departmentName(pd.getDepartment().getDepartmentName());
						if (pd.getDepartment().getCollege() != null) {
							builder.collegeName(pd.getDepartment().getCollege().getCollegeName());
						}
					});
		}
	}
	
	private String decryptIfNotNull(String encryptedValue) {
		if (encryptedValue == null || encryptedValue.isEmpty()) {
			return null;
		}
		return encryptionService.decryptPhoneNumber(encryptedValue);
	}
}
