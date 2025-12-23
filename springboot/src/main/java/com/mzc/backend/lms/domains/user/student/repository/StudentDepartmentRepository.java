package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학생-학과 관계 Repository
 */
@Repository
public interface StudentDepartmentRepository extends JpaRepository<StudentDepartment, Long> {
	
	/**
	 * 학생으로 학과 정보 조회
	 */
	Optional<StudentDepartment> findByStudent(Student student);
	
	/**
	 * 학생 ID로 학과 정보 조회
	 */
	@Query("SELECT sd FROM StudentDepartment sd WHERE sd.student.studentId = :studentId")
	Optional<StudentDepartment> findByStudentId(@Param("studentId") Long studentId);
	
	/**
	 * 학과별 학생 목록 조회
	 */
	List<StudentDepartment> findByDepartment(Department department);
}
