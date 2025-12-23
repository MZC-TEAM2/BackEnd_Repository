package com.mzc.backend.lms.domains.user.profile.controller;

import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;
import com.mzc.backend.lms.domains.user.profile.service.ProfileImageService;
import com.mzc.backend.lms.domains.user.profile.service.ProfileService;
import com.mzc.backend.lms.domains.user.user.exceptions.UserErrorCode;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProfileController 테스트")
class ProfileControllerTest {
	
	@Mock
	private ProfileService profileService;
	
	@Mock
	private ProfileImageService profileImageService;
	
	private ProfileController controller;
	
	@BeforeEach
	void setUp() {
		controller = new ProfileController(profileService, profileImageService);
	}
	
	private ProfileResponseDto createStudentProfileResponse(Long userId) {
		return ProfileResponseDto.builder()
				.userId(userId)
				.email("student@test.com")
				.name("홍길동")
				.mobileNumber("010-1234-5678")
				.mobileVerified(true)
				.profileImageUrl("http://image.url/profile.jpg")
				.userType("STUDENT")
				.studentId(userId)
				.admissionYear(2025)
				.grade(1)
				.collegeName("공과대학")
				.departmentName("컴퓨터공학과")
				.build();
	}
	
	private ProfileResponseDto createProfessorProfileResponse(Long userId) {
		return ProfileResponseDto.builder()
				.userId(userId)
				.email("professor@test.com")
				.name("김교수")
				.mobileNumber("010-9876-5432")
				.mobileVerified(true)
				.userType("PROFESSOR")
				.professorId(userId)
				.appointmentDate(LocalDate.of(2020, 3, 1))
				.collegeName("공과대학")
				.departmentName("컴퓨터공학과")
				.build();
	}
	
	// Helper methods
	
	@Nested
	@DisplayName("내 프로필 조회 API")
	class GetMyProfile {
		
		@Test
		@DisplayName("학생 프로필 조회 성공")
		void getStudentProfileSuccess() {
			// given
			Long userId = 2025010001L;
			ProfileResponseDto mockResponse = createStudentProfileResponse(userId);
			
			when(profileService.getMyProfile(userId)).thenReturn(mockResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.getMyProfile(userId);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getUserId()).isEqualTo(userId);
			assertThat(response.getBody().getUserType()).isEqualTo("STUDENT");
			assertThat(response.getBody().getStudentId()).isEqualTo(userId);
			assertThat(response.getBody().getDepartmentName()).isEqualTo("컴퓨터공학과");
			verify(profileService).getMyProfile(userId);
		}
		
		@Test
		@DisplayName("교수 프로필 조회 성공")
		void getProfessorProfileSuccess() {
			// given
			Long userId = 1001L;
			ProfileResponseDto mockResponse = createProfessorProfileResponse(userId);
			
			when(profileService.getMyProfile(userId)).thenReturn(mockResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.getMyProfile(userId);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getUserId()).isEqualTo(userId);
			assertThat(response.getBody().getUserType()).isEqualTo("PROFESSOR");
			assertThat(response.getBody().getProfessorId()).isEqualTo(userId);
			assertThat(response.getBody().getAppointmentDate()).isEqualTo(LocalDate.of(2020, 3, 1));
		}
		
		@Test
		@DisplayName("존재하지 않는 사용자 조회 실패")
		void getUserNotFound() {
			// given
			Long userId = 9999L;
			
			when(profileService.getMyProfile(userId))
					.thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));
			
			// when & then
			try {
				controller.getMyProfile(userId);
			} catch (UserException e) {
				assertThat(e.getUserErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
			}
			verify(profileService).getMyProfile(userId);
		}
		
		@Test
		@DisplayName("프로필 이미지 포함 조회")
		void getProfileWithImage() {
			// given
			Long userId = 100L;
			ProfileResponseDto mockResponse = ProfileResponseDto.builder()
					.userId(userId)
					.email("user@test.com")
					.name("테스트")
					.profileImageUrl("http://image.url/profile.jpg")
					.thumbnailUrl("http://image.url/thumbnail.jpg")
					.build();
			
			when(profileService.getMyProfile(userId)).thenReturn(mockResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.getMyProfile(userId);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getProfileImageUrl()).isEqualTo("http://image.url/profile.jpg");
			assertThat(response.getBody().getThumbnailUrl()).isEqualTo("http://image.url/thumbnail.jpg");
		}
		
		@Test
		@DisplayName("연락처 정보 포함 조회")
		void getProfileWithContact() {
			// given
			Long userId = 100L;
			ProfileResponseDto mockResponse = ProfileResponseDto.builder()
					.userId(userId)
					.email("user@test.com")
					.name("테스트")
					.mobileNumber("010-1234-5678")
					.homeNumber("02-1234-5678")
					.officeNumber("02-9876-5432")
					.mobileVerified(true)
					.build();
			
			when(profileService.getMyProfile(userId)).thenReturn(mockResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.getMyProfile(userId);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getMobileNumber()).isEqualTo("010-1234-5678");
			assertThat(response.getBody().getHomeNumber()).isEqualTo("02-1234-5678");
			assertThat(response.getBody().getOfficeNumber()).isEqualTo("02-9876-5432");
			assertThat(response.getBody().getMobileVerified()).isTrue();
		}
	}
	
	@Nested
	@DisplayName("프로필 수정 API")
	class UpdateProfile {
		
		@Test
		@DisplayName("프로필 수정 성공")
		void updateProfileSuccess() {
			// given
			Long userId = 100L;
			ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
					.name("새이름")
					.mobileNumber("010-9999-8888")
					.build();
			
			ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
					.userId(userId)
					.email("user@test.com")
					.name("새이름")
					.mobileNumber("010-9999-8888")
					.build();
			
			when(profileService.updateProfile(userId, request)).thenReturn(expectedResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.updateProfile(userId, request);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getName()).isEqualTo("새이름");
			assertThat(response.getBody().getMobileNumber()).isEqualTo("010-9999-8888");
			verify(profileService).updateProfile(userId, request);
		}
		
		@Test
		@DisplayName("프로필 수정 실패 - 사용자 없음")
		void updateProfileUserNotFound() {
			// given
			Long userId = 9999L;
			ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
					.name("새이름")
					.build();
			
			doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
					.when(profileService).updateProfile(userId, request);
			
			// when & then
			try {
				controller.updateProfile(userId, request);
			} catch (UserException e) {
				assertThat(e.getUserErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
			}
			verify(profileService).updateProfile(userId, request);
		}
		
		@Test
		@DisplayName("이름만 수정")
		void updateProfileNameOnly() {
			// given
			Long userId = 100L;
			ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
					.name("새이름")
					.build();
			
			ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
					.userId(userId)
					.email("user@test.com")
					.name("새이름")
					.build();
			
			when(profileService.updateProfile(userId, request)).thenReturn(expectedResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.updateProfile(userId, request);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getName()).isEqualTo("새이름");
			verify(profileService).updateProfile(userId, request);
		}
		
		@Test
		@DisplayName("연락처만 수정")
		void updateProfileContactOnly() {
			// given
			Long userId = 100L;
			ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
					.mobileNumber("010-9999-8888")
					.homeNumber("02-1111-2222")
					.officeNumber("02-3333-4444")
					.build();
			
			ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
					.userId(userId)
					.email("user@test.com")
					.mobileNumber("010-9999-8888")
					.homeNumber("02-1111-2222")
					.officeNumber("02-3333-4444")
					.build();
			
			when(profileService.updateProfile(userId, request)).thenReturn(expectedResponse);
			
			// when
			ResponseEntity<ProfileResponseDto> response = controller.updateProfile(userId, request);
			
			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMobileNumber()).isEqualTo("010-9999-8888");
			verify(profileService).updateProfile(userId, request);
		}
	}
}
