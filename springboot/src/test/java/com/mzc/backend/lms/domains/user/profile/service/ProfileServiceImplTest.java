package com.mzc.backend.lms.domains.user.profile.service;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.organization.entity.College;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;
import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment;
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProfileServiceImpl 테스트")
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserPrimaryContactRepository userPrimaryContactRepository;
    @Mock
    private UserProfileImageRepository userProfileImageRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentDepartmentRepository studentDepartmentRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private ProfessorDepartmentRepository professorDepartmentRepository;
    @Mock
    private EncryptionService encryptionService;

    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileServiceImpl(
                userRepository,
                userProfileRepository,
                userPrimaryContactRepository,
                userProfileImageRepository,
                studentRepository,
                studentDepartmentRepository,
                professorRepository,
                professorDepartmentRepository,
                encryptionService
        );
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("학생 프로필 조회 성공")
        void getStudentProfileSuccess() {
            // given
            Long userId = 2025010001L;
            User mockUser = createMockUser(userId, "encrypted_email");
            Student mockStudent = createMockStudent(userId);
            UserProfile mockProfile = createMockUserProfile("encrypted_name");
            UserPrimaryContact mockContact = createMockUserPrimaryContact("encrypted_mobile");
            UserProfileImage mockImage = createMockUserProfileImage("http://image.url/profile.jpg");
            StudentDepartment mockStudentDept = createMockStudentDepartment(mockStudent);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.of(mockContact));
            when(userProfileImageRepository.findByUserId(userId)).thenReturn(Optional.of(mockImage));
            when(studentRepository.findById(userId)).thenReturn(Optional.of(mockStudent));
            when(studentDepartmentRepository.findByStudentId(userId)).thenReturn(Optional.of(mockStudentDept));
            when(professorRepository.findById(userId)).thenReturn(Optional.empty());

            when(encryptionService.decryptEmail("encrypted_email")).thenReturn("student@test.com");
            when(encryptionService.decryptName("encrypted_name")).thenReturn("홍길동");
            when(encryptionService.decryptPhoneNumber("encrypted_mobile")).thenReturn("010-1234-5678");

            // when
            ProfileResponseDto result = profileService.getMyProfile(userId);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("student@test.com");
            assertThat(result.getName()).isEqualTo("홍길동");
            assertThat(result.getMobileNumber()).isEqualTo("010-1234-5678");
            assertThat(result.getProfileImageUrl()).isEqualTo("http://image.url/profile.jpg");
            assertThat(result.getUserType()).isEqualTo("STUDENT");
            assertThat(result.getStudentId()).isEqualTo(userId);
            assertThat(result.getDepartmentName()).isEqualTo("컴퓨터공학과");
            assertThat(result.getCollegeName()).isEqualTo("공과대학");
        }

        @Test
        @DisplayName("교수 프로필 조회 성공")
        void getProfessorProfileSuccess() {
            // given
            Long userId = 1001L;
            User mockUser = createMockUser(userId, "encrypted_email");
            Professor mockProfessor = createMockProfessor(userId);
            UserProfile mockProfile = createMockUserProfile("encrypted_name");
            ProfessorDepartment mockProfessorDept = createMockProfessorDepartment(mockProfessor);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userProfileImageRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(studentRepository.findById(userId)).thenReturn(Optional.empty());
            when(professorRepository.findById(userId)).thenReturn(Optional.of(mockProfessor));
            when(professorDepartmentRepository.findByProfessorId(userId)).thenReturn(Optional.of(mockProfessorDept));

            when(encryptionService.decryptEmail("encrypted_email")).thenReturn("professor@test.com");
            when(encryptionService.decryptName("encrypted_name")).thenReturn("김교수");

            // when
            ProfileResponseDto result = profileService.getMyProfile(userId);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("professor@test.com");
            assertThat(result.getName()).isEqualTo("김교수");
            assertThat(result.getUserType()).isEqualTo("PROFESSOR");
            assertThat(result.getProfessorId()).isEqualTo(userId);
            assertThat(result.getAppointmentDate()).isEqualTo(LocalDate.of(2020, 3, 1));
            assertThat(result.getDepartmentName()).isEqualTo("컴퓨터공학과");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 실패")
        void getUserNotFound() {
            // given
            Long userId = 9999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getMyProfile(userId))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("프로필 정보 없이 기본 정보만 조회")
        void getProfileWithMinimalInfo() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userProfileImageRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(studentRepository.findById(userId)).thenReturn(Optional.empty());
            when(professorRepository.findById(userId)).thenReturn(Optional.empty());

            when(encryptionService.decryptEmail("encrypted_email")).thenReturn("user@test.com");

            // when
            ProfileResponseDto result = profileService.getMyProfile(userId);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("user@test.com");
            assertThat(result.getName()).isNull();
            assertThat(result.getMobileNumber()).isNull();
            assertThat(result.getUserType()).isNull();
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("프로필 수정 성공 - 이름만 변경")
        void updateProfileNameSuccess() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");
            UserProfile mockProfile = mock(UserProfile.class);

            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .name("새이름")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));
            when(encryptionService.encryptName("새이름")).thenReturn("encrypted_new_name");

            // when
            profileService.updateProfile(userId, request);

            // then
            verify(mockProfile).changeName("encrypted_new_name");
            verify(userProfileRepository).save(mockProfile);
        }

        @Test
        @DisplayName("프로필 수정 성공 - 연락처만 변경")
        void updateProfileContactSuccess() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");
            UserPrimaryContact mockContact = mock(UserPrimaryContact.class);

            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .mobileNumber("010-9999-8888")
                    .homeNumber("02-1111-2222")
                    .officeNumber("02-3333-4444")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.of(mockContact));
            when(encryptionService.encryptPhoneNumber("010-9999-8888")).thenReturn("encrypted_mobile");
            when(encryptionService.encryptPhoneNumber("02-1111-2222")).thenReturn("encrypted_home");
            when(encryptionService.encryptPhoneNumber("02-3333-4444")).thenReturn("encrypted_office");

            // when
            profileService.updateProfile(userId, request);

            // then
            verify(mockContact).updateMobileNumber("encrypted_mobile");
            verify(mockContact).updateHomeNumber("encrypted_home");
            verify(mockContact).updateOfficeNumber("encrypted_office");
            verify(userPrimaryContactRepository).save(mockContact);
        }

        @Test
        @DisplayName("프로필 수정 성공 - 이름과 연락처 모두 변경")
        void updateProfileAllSuccess() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");
            UserProfile mockProfile = mock(UserProfile.class);
            UserPrimaryContact mockContact = mock(UserPrimaryContact.class);

            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .name("새이름")
                    .mobileNumber("010-9999-8888")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.of(mockContact));
            when(encryptionService.encryptName("새이름")).thenReturn("encrypted_new_name");
            when(encryptionService.encryptPhoneNumber("010-9999-8888")).thenReturn("encrypted_mobile");

            // when
            profileService.updateProfile(userId, request);

            // then
            verify(mockProfile).changeName("encrypted_new_name");
            verify(mockContact).updateMobileNumber("encrypted_mobile");
            verify(userProfileRepository).save(mockProfile);
            verify(userPrimaryContactRepository).save(mockContact);
        }

        @Test
        @DisplayName("프로필 수정 실패 - 사용자 없음")
        void updateProfileUserNotFound() {
            // given
            Long userId = 9999L;
            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .name("새이름")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.updateProfile(userId, request))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("프로필 수정 - 프로필 없으면 생성")
        void updateProfileCreateIfNotExists() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");

            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .name("새이름")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(encryptionService.encryptName("새이름")).thenReturn("encrypted_new_name");

            // when
            profileService.updateProfile(userId, request);

            // then
            verify(userProfileRepository).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("프로필 수정 - 연락처 없으면 생성")
        void updateContactCreateIfNotExists() {
            // given
            Long userId = 100L;
            User mockUser = createMockUser(userId, "encrypted_email");

            ProfileUpdateRequestDto request = ProfileUpdateRequestDto.builder()
                    .mobileNumber("010-9999-8888")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userPrimaryContactRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(encryptionService.encryptPhoneNumber("010-9999-8888")).thenReturn("encrypted_mobile");

            // when
            profileService.updateProfile(userId, request);

            // then
            verify(userPrimaryContactRepository).save(any(UserPrimaryContact.class));
        }
    }

    // Helper methods for creating mock objects

    private User createMockUser(Long userId, String email) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        return user;
    }

    private Student createMockStudent(Long studentId) {
        Student student = mock(Student.class);
        when(student.getStudentId()).thenReturn(studentId);
        when(student.getAdmissionYear()).thenReturn(2025);
        when(student.getGrade()).thenReturn(1);
        return student;
    }

    private Professor createMockProfessor(Long professorId) {
        Professor professor = mock(Professor.class);
        when(professor.getProfessorId()).thenReturn(professorId);
        when(professor.getAppointmentDate()).thenReturn(LocalDate.of(2020, 3, 1));
        return professor;
    }

    private UserProfile createMockUserProfile(String name) {
        UserProfile profile = mock(UserProfile.class);
        when(profile.getName()).thenReturn(name);
        return profile;
    }

    private UserPrimaryContact createMockUserPrimaryContact(String mobileNumber) {
        UserPrimaryContact contact = mock(UserPrimaryContact.class);
        when(contact.getMobileNumber()).thenReturn(mobileNumber);
        when(contact.getHomeNumber()).thenReturn(null);
        when(contact.getOfficeNumber()).thenReturn(null);
        when(contact.getMobileVerified()).thenReturn(true);
        return contact;
    }

    private UserProfileImage createMockUserProfileImage(String imageUrl) {
        UserProfileImage image = mock(UserProfileImage.class);
        when(image.getImageUrl()).thenReturn(imageUrl);
        when(image.getThumbnailUrl()).thenReturn(null);
        return image;
    }

    private StudentDepartment createMockStudentDepartment(Student student) {
        College college = mock(College.class);
        when(college.getCollegeName()).thenReturn("공과대학");

        Department department = mock(Department.class);
        when(department.getDepartmentName()).thenReturn("컴퓨터공학과");
        when(department.getCollege()).thenReturn(college);

        StudentDepartment studentDept = mock(StudentDepartment.class);
        when(studentDept.getStudent()).thenReturn(student);
        when(studentDept.getDepartment()).thenReturn(department);
        return studentDept;
    }

    private ProfessorDepartment createMockProfessorDepartment(Professor professor) {
        College college = mock(College.class);
        when(college.getCollegeName()).thenReturn("공과대학");

        Department department = mock(Department.class);
        when(department.getDepartmentName()).thenReturn("컴퓨터공학과");
        when(department.getCollege()).thenReturn(college);

        ProfessorDepartment professorDept = mock(ProfessorDepartment.class);
        when(professorDept.getProfessor()).thenReturn(professor);
        when(professorDept.getDepartment()).thenReturn(department);
        return professorDept;
    }
}
