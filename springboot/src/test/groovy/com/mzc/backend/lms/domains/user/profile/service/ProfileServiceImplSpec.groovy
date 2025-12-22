package com.mzc.backend.lms.domains.user.profile.service

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto
import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository
import com.mzc.backend.lms.domains.user.student.entity.Student
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository
import com.mzc.backend.lms.domains.user.user.entity.User
import com.mzc.backend.lms.domains.user.user.exceptions.UserException
import com.mzc.backend.lms.domains.user.user.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

/**
 * ProfileServiceImpl 테스트
 * 프로필 조회 및 업데이트 기능 테스트
 */
class ProfileServiceImplSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def userProfileRepository = Mock(UserProfileRepository)
    def userPrimaryContactRepository = Mock(UserPrimaryContactRepository)
    def userProfileImageRepository = Mock(UserProfileImageRepository)
    def studentRepository = Mock(StudentRepository)
    def studentDepartmentRepository = Mock(StudentDepartmentRepository)
    def professorRepository = Mock(ProfessorRepository)
    def professorDepartmentRepository = Mock(ProfessorDepartmentRepository)
    def encryptionService = Mock(EncryptionService)

    @Subject
    def profileService = new ProfileServiceImpl(
            userRepository,
            userProfileRepository,
            userPrimaryContactRepository,
            userProfileImageRepository,
            studentRepository,
            studentDepartmentRepository,
            professorRepository,
            professorDepartmentRepository,
            encryptionService
    )

    def "존재하지 않는 사용자 프로필 조회 시 예외가 발생한다"() {
        given: "존재하지 않는 사용자 ID"
        def userId = 999L
        userRepository.findById(userId) >> Optional.empty()

        when: "프로필 조회를 요청하면"
        profileService.getMyProfile(userId)

        then: "UserException이 발생한다"
        thrown(UserException)
    }

    def "학생 사용자의 프로필을 조회한다"() {
        given: "학생 사용자"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def profile = Mock(UserProfile) {
            getName() >> "encrypted_name"
        }
        def contact = Mock(UserPrimaryContact) {
            getMobileNumber() >> "encrypted_mobile"
            getHomeNumber() >> null
            getOfficeNumber() >> null
            getMobileVerified() >> true
        }
        def profileImage = Mock(UserProfileImage) {
            getImageUrl() >> "http://example.com/image.jpg"
            getThumbnailUrl() >> "http://example.com/thumb.jpg"
        }
        def student = Mock(Student) {
            getStudentId() >> "S001"
            getAdmissionYear() >> 2025
            getGrade() >> 1
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.of(profile)
        userPrimaryContactRepository.findByUserId(userId) >> Optional.of(contact)
        userProfileImageRepository.findByUserId(userId) >> Optional.of(profileImage)
        studentRepository.findById(userId) >> Optional.of(student)
        studentDepartmentRepository.findByStudentId(userId) >> Optional.empty()

        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"
        encryptionService.decryptName("encrypted_name") >> "홍길동"
        encryptionService.decryptPhoneNumber("encrypted_mobile") >> "010-1234-5678"

        when: "프로필을 조회하면"
        def result = profileService.getMyProfile(userId)

        then: "학생 프로필 정보가 반환된다"
        result.userId == userId
        result.email == "test@example.com"
        result.name == "홍길동"
        result.mobileNumber == "010-1234-5678"
        result.profileImageUrl == "http://example.com/image.jpg"
        result.userType == "STUDENT"
        result.admissionYear == 2025
    }

    def "교수 사용자의 프로필을 조회한다"() {
        given: "교수 사용자"
        def userId = 2L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_professor_email"
        }
        def professor = Mock(Professor) {
            getProfessorId() >> "P001"
            getAppointmentDate() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.of(professor)
        professorDepartmentRepository.findByProfessorId(userId) >> Optional.empty()

        encryptionService.decryptEmail("encrypted_professor_email") >> "professor@example.com"

        when: "프로필을 조회하면"
        def result = profileService.getMyProfile(userId)

        then: "교수 프로필 정보가 반환된다"
        result.userId == userId
        result.email == "professor@example.com"
        result.userType == "PROFESSOR"
        result.professorId == "P001"
    }

    def "프로필 정보가 없는 사용자도 기본 정보를 조회할 수 있다"() {
        given: "프로필 정보가 없는 사용자"
        def userId = 3L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.decryptEmail("encrypted_email") >> "user@example.com"

        when: "프로필을 조회하면"
        def result = profileService.getMyProfile(userId)

        then: "기본 정보만 반환된다"
        result.userId == userId
        result.email == "user@example.com"
        result.name == null
        result.userType == null
    }

    def "이름을 업데이트한다 - 기존 프로필 존재"() {
        given: "기존 프로필이 있는 사용자"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def existingProfile = Mock(UserProfile)
        def request = Mock(ProfileUpdateRequestDto) {
            getName() >> "새이름"
            getMobileNumber() >> null
            getHomeNumber() >> null
            getOfficeNumber() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.of(existingProfile)
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.encryptName("새이름") >> "encrypted_new_name"
        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"

        when: "프로필을 업데이트하면"
        profileService.updateProfile(userId, request)

        then: "기존 프로필의 이름이 변경된다"
        1 * existingProfile.changeName("encrypted_new_name")
        1 * userProfileRepository.save(existingProfile)
    }

    def "이름을 업데이트한다 - 새 프로필 생성"() {
        given: "프로필이 없는 사용자"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def request = Mock(ProfileUpdateRequestDto) {
            getName() >> "새이름"
            getMobileNumber() >> null
            getHomeNumber() >> null
            getOfficeNumber() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.encryptName("새이름") >> "encrypted_new_name"
        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"

        when: "프로필을 업데이트하면"
        profileService.updateProfile(userId, request)

        then: "새 프로필이 생성된다"
        1 * userProfileRepository.save(_)
    }

    def "연락처를 업데이트한다 - 기존 연락처 존재"() {
        given: "기존 연락처가 있는 사용자"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def existingContact = Mock(UserPrimaryContact)
        def request = Mock(ProfileUpdateRequestDto) {
            getName() >> null
            getMobileNumber() >> "010-9999-8888"
            getHomeNumber() >> null
            getOfficeNumber() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.of(existingContact)
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.encryptPhoneNumber("010-9999-8888") >> "encrypted_mobile"
        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"

        when: "연락처를 업데이트하면"
        profileService.updateProfile(userId, request)

        then: "기존 연락처가 업데이트된다"
        1 * existingContact.updateMobileNumber("encrypted_mobile")
        1 * userPrimaryContactRepository.save(existingContact)
    }

    def "연락처를 업데이트한다 - 새 연락처 생성"() {
        given: "연락처가 없는 사용자"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def request = Mock(ProfileUpdateRequestDto) {
            getName() >> null
            getMobileNumber() >> "010-1111-2222"
            getHomeNumber() >> "02-111-2222"
            getOfficeNumber() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.encryptPhoneNumber("010-1111-2222") >> "encrypted_mobile"
        encryptionService.encryptPhoneNumber("02-111-2222") >> "encrypted_home"
        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"

        when: "연락처를 업데이트하면"
        profileService.updateProfile(userId, request)

        then: "새 연락처가 생성된다"
        1 * userPrimaryContactRepository.save(_)
    }

    def "빈 이름은 업데이트하지 않는다"() {
        given: "빈 이름으로 업데이트 요청"
        def userId = 1L
        def user = Mock(User) {
            getId() >> userId
            getEmail() >> "encrypted_email"
        }
        def request = Mock(ProfileUpdateRequestDto) {
            getName() >> ""
            getMobileNumber() >> null
            getHomeNumber() >> null
            getOfficeNumber() >> null
        }

        userRepository.findById(userId) >> Optional.of(user)
        userProfileRepository.findByUserId(userId) >> Optional.empty()
        userPrimaryContactRepository.findByUserId(userId) >> Optional.empty()
        userProfileImageRepository.findByUserId(userId) >> Optional.empty()
        studentRepository.findById(userId) >> Optional.empty()
        professorRepository.findById(userId) >> Optional.empty()

        encryptionService.decryptEmail("encrypted_email") >> "test@example.com"

        when: "프로필을 업데이트하면"
        profileService.updateProfile(userId, request)

        then: "프로필 저장이 호출되지 않는다"
        0 * userProfileRepository.save(_)
    }

    def "존재하지 않는 사용자 프로필 업데이트 시 예외가 발생한다"() {
        given: "존재하지 않는 사용자 ID"
        def userId = 999L
        def request = Mock(ProfileUpdateRequestDto)
        userRepository.findById(userId) >> Optional.empty()

        when: "프로필 업데이트를 요청하면"
        profileService.updateProfile(userId, request)

        then: "UserException이 발생한다"
        thrown(UserException)
    }
}
