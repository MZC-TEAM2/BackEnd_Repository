package com.mzc.backend.lms.domains.course.course.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.course.dto.CreateCourseRequestDto;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseTypeRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("교수 강의 관리 서비스 테스트")
class ProfessorCourseServiceTest {
	
	@InjectMocks
	private ProfessorCourseService professorCourseService;
	
	@Mock
	private CourseRepository courseRepository;
	@Mock
	private SubjectRepository subjectRepository;
	@Mock
	private AcademicTermRepository academicTermRepository;
	@Mock
	private ProfessorRepository professorRepository;
	@Mock
	private ProfessorDepartmentRepository professorDepartmentRepository;
	@Mock
	private EnrollmentRepository enrollmentRepository;
	@Mock
	private EnrollmentPeriodRepository enrollmentPeriodRepository;
	@Mock
	private CourseService courseService;
	@Mock
	private CourseTypeRepository courseTypeRepository;
	@Mock
	private CourseWeekRepository courseWeekRepository;
	
	@Test
	@DisplayName("강의 취소는 강의 등록 기간이 아닐 때 실패한다")
	void cancelCourse_fails_whenNotCourseRegistrationPeriod() {
		// given
		Long courseId = 1L;
		Long professorId = 10L;
		
		AcademicTerm term = AcademicTerm.builder()
				.id(300L)
				.year(2025)
				.termType("1")
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(4))
				.build();
		
		Professor professor = Professor.create(professorId, null, LocalDate.now());
		
		Subject subject = org.mockito.Mockito.mock(Subject.class);
		
		Course course = Course.builder()
				.id(courseId)
				.subject(subject)
				.professor(professor)
				.academicTerm(term)
				.sectionNumber("01")
				.maxStudents(30)
				.currentStudents(0)
				.description("")
				.build();
		
		when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
		when(enrollmentPeriodRepository.existsActiveCourseRegistrationPeriodByAcademicTermId(
				anyLong(), any(LocalDateTime.class)))
				.thenReturn(false);
		
		// when & then
		assertThatThrownBy(() -> professorCourseService.cancelCourse(courseId, professorId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("강의 등록 기간이 아닙니다.");
		
		verify(courseRepository, never()).delete(any());
	}
	
	@Test
	@DisplayName("과목당 학기별 강의(분반)는 최대 2개까지만 개설 가능하다")
	void createCourse_fails_whenExceedsMaxCoursesPerSubjectInTerm() {
		// given
		Long professorId = 10L;
		Long enrollmentPeriodId = 100L;
		Long subjectId = 200L;
		
		AcademicTerm term = AcademicTerm.builder()
				.id(300L)
				.year(2025)
				.termType("1")
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(4))
				.build();
		
		EnrollmentPeriod enrollmentPeriod = EnrollmentPeriod.builder()
				.id(enrollmentPeriodId)
				.academicTerm(term)
				.periodName("강의등록기간")
				.startDatetime(LocalDateTime.now().minusDays(1))
				.endDatetime(LocalDateTime.now().plusDays(1))
				.targetYear(0)
				.build();
		
		Professor professor = Professor.create(professorId, null, LocalDate.now());
		
		Department department = org.mockito.Mockito.mock(Department.class);
		when(department.getId()).thenReturn(999L);
		when(department.getDepartmentName()).thenReturn("테스트학과");
		
		ProfessorDepartment professorDepartment = ProfessorDepartment.create(
				professor, department, true, LocalDate.now().minusYears(1));
		
		CourseType courseType = org.mockito.Mockito.mock(CourseType.class);
		when(courseType.getCategory()).thenReturn(1); // 교양으로 처리해 학과 검증 스킵
		
		Subject subject = Subject.builder()
				.subjectCode("CS101")
				.subjectName("자료구조")
				.subjectDescription("과목")
				.department(department)
				.courseType(courseType)
				.credits(3)
				.description("desc")
				.build();
		subject.setId(subjectId);
		
		CreateCourseRequestDto request = CreateCourseRequestDto.builder()
				.enrollmentPeriodId(enrollmentPeriodId)
				.subjectId(subjectId)
				.section("01")
				.maxStudents(30)
				.schedule(null)
				.build();
		
		when(enrollmentPeriodRepository.findById(enrollmentPeriodId)).thenReturn(Optional.of(enrollmentPeriod));
		when(professorRepository.findById(professorId)).thenReturn(Optional.of(professor));
		when(professorDepartmentRepository.findByProfessorId(professorId)).thenReturn(Optional.of(professorDepartment));
		when(subjectRepository.findByIdWithLock(subjectId)).thenReturn(Optional.of(subject));
		when(courseRepository.countBySubjectIdAndAcademicTermId(subjectId, term.getId())).thenReturn(2L);
		
		// when & then
		assertThatThrownBy(() -> professorCourseService.createCourse(request, professorId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("해당 과목은 한 학기 최대 2개 강의(분반)까지만 개설할 수 있습니다.");
		
		verify(courseRepository, never()).save(any());
		verify(courseWeekRepository, never()).save(any());
	}
}


