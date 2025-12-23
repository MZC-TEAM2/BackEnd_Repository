package com.mzc.backend.lms.domains.user.search.service;

import com.mzc.backend.lms.domains.user.organization.repository.CollegeRepository;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.search.dto.*;
import com.mzc.backend.lms.domains.user.search.dto.UserSearchRequestDto.SortBy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 유저 탐색 서비스 (커서 기반 페이징)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {
	
	private final EntityManager entityManager;
	private final CollegeRepository collegeRepository;
	private final DepartmentRepository departmentRepository;
	
	/**
	 * 유저 탐색 (커서 기반)
	 */
	public UserSearchCursorResponseDto searchUsers(UserSearchRequestDto request) {
		int size = request.getSize() != null ? request.getSize() : 20;
		int fetchSize = size + 1;
		SortBy sortBy = request.getSortBy() != null ? request.getSortBy() : SortBy.ID;
		
		List<UserSearchResponseDto> results = new ArrayList<>();
		
		if (request.getUserType() == null || request.getUserType() == UserSearchRequestDto.UserType.STUDENT) {
			results.addAll(searchStudents(request, fetchSize, sortBy));
		}
		
		if (request.getUserType() == null || request.getUserType() == UserSearchRequestDto.UserType.PROFESSOR) {
			results.addAll(searchProfessors(request, fetchSize, sortBy));
		}
		
		if (sortBy == SortBy.NAME) {
			results.sort(Comparator.comparing(UserSearchResponseDto::getName, Comparator.nullsLast(Comparator.naturalOrder()))
					.thenComparing(UserSearchResponseDto::getUserId));
		} else {
			results.sort(Comparator.comparing(UserSearchResponseDto::getUserId));
		}
		
		if (results.size() > fetchSize) {
			results = results.subList(0, fetchSize);
		}
		
		return UserSearchCursorResponseDto.of(results, size, sortBy);
	}
	
	private List<UserSearchResponseDto> searchStudents(UserSearchRequestDto request, int fetchSize, SortBy sortBy) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("SELECT new com.mzc.backend.lms.domains.user.search.dto.UserSearchResponseDto(");
		jpql.append("s.studentId, up.name, u.email, d.departmentName, c.collegeName, 'STUDENT', upi.thumbnailUrl) ");
		jpql.append("FROM Student s ");
		jpql.append("JOIN s.user u ");
		jpql.append("LEFT JOIN UserProfile up ON up.user = u ");
		jpql.append("LEFT JOIN UserProfileImage upi ON upi.user = u ");
		jpql.append("LEFT JOIN StudentDepartment sd ON sd.student = s AND sd.isPrimary = true ");
		jpql.append("LEFT JOIN sd.department d ");
		jpql.append("LEFT JOIN d.college c ");
		jpql.append("WHERE u.deletedAt IS NULL ");
		
		Map<String, Object> params = new HashMap<>();
		
		appendCursorCondition(jpql, params, request, sortBy, "s.studentId");
		appendFilters(jpql, params, request);
		appendOrderBy(jpql, sortBy, "s.studentId");
		
		TypedQuery<UserSearchResponseDto> query = entityManager.createQuery(jpql.toString(), UserSearchResponseDto.class);
		params.forEach(query::setParameter);
		query.setMaxResults(fetchSize);
		
		return query.getResultList();
	}
	
	private List<UserSearchResponseDto> searchProfessors(UserSearchRequestDto request, int fetchSize, SortBy sortBy) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("SELECT new com.mzc.backend.lms.domains.user.search.dto.UserSearchResponseDto(");
		jpql.append("p.professorId, up.name, u.email, d.departmentName, c.collegeName, 'PROFESSOR', upi.thumbnailUrl) ");
		jpql.append("FROM Professor p ");
		jpql.append("JOIN p.user u ");
		jpql.append("LEFT JOIN UserProfile up ON up.user = u ");
		jpql.append("LEFT JOIN UserProfileImage upi ON upi.user = u ");
		jpql.append("LEFT JOIN ProfessorDepartment pd ON pd.professor = p AND pd.isPrimary = true ");
		jpql.append("LEFT JOIN pd.department d ");
		jpql.append("LEFT JOIN d.college c ");
		jpql.append("WHERE u.deletedAt IS NULL ");
		
		Map<String, Object> params = new HashMap<>();
		
		appendCursorCondition(jpql, params, request, sortBy, "p.professorId");
		appendFilters(jpql, params, request);
		appendOrderBy(jpql, sortBy, "p.professorId");
		
		TypedQuery<UserSearchResponseDto> query = entityManager.createQuery(jpql.toString(), UserSearchResponseDto.class);
		params.forEach(query::setParameter);
		query.setMaxResults(fetchSize);
		
		return query.getResultList();
	}
	
	private void appendCursorCondition(StringBuilder jpql, Map<String, Object> params,
	                                   UserSearchRequestDto request, SortBy sortBy, String idField) {
		if (sortBy == SortBy.NAME) {
			if (request.getCursorName() != null && request.getCursorId() != null) {
				jpql.append("AND (up.name > :cursorName OR (up.name = :cursorName AND ");
				jpql.append(idField).append(" > :cursorId)) ");
				params.put("cursorName", request.getCursorName());
				params.put("cursorId", request.getCursorId());
			} else if (request.getCursorName() != null) {
				jpql.append("AND up.name > :cursorName ");
				params.put("cursorName", request.getCursorName());
			}
		} else {
			if (request.getCursorId() != null) {
				jpql.append("AND ").append(idField).append(" > :cursorId ");
				params.put("cursorId", request.getCursorId());
			}
		}
	}
	
	private void appendFilters(StringBuilder jpql, Map<String, Object> params, UserSearchRequestDto request) {
		if (request.getCollegeId() != null) {
			jpql.append("AND c.id = :collegeId ");
			params.put("collegeId", request.getCollegeId());
		}
		
		if (request.getDepartmentId() != null) {
			jpql.append("AND d.id = :departmentId ");
			params.put("departmentId", request.getDepartmentId());
		}
		
		if (request.getName() != null && !request.getName().isBlank()) {
			jpql.append("AND up.name LIKE :name ");
			params.put("name", "%" + request.getName() + "%");
		}
	}
	
	private void appendOrderBy(StringBuilder jpql, SortBy sortBy, String idField) {
		if (sortBy == SortBy.NAME) {
			jpql.append("ORDER BY up.name ASC, ").append(idField).append(" ASC");
		} else {
			jpql.append("ORDER BY ").append(idField).append(" ASC");
		}
	}
	
	/**
	 * 전체 단과대 목록 조회 (필터 선택용)
	 */
	public List<CollegeListResponseDto> getAllColleges() {
		return collegeRepository.findAll().stream()
				.map(CollegeListResponseDto::from)
				.toList();
	}
	
	/**
	 * 단과대별 학과 목록 조회 (필터 선택용)
	 */
	public List<DepartmentListResponseDto> getDepartmentsByCollegeId(Long collegeId) {
		return departmentRepository.findByCollegeId(collegeId).stream()
				.map(DepartmentListResponseDto::from)
				.toList();
	}
	
	/**
	 * 전체 학과 목록 조회 (필터 선택용)
	 */
	public List<DepartmentListResponseDto> getAllDepartments() {
		return departmentRepository.findAll().stream()
				.map(DepartmentListResponseDto::from)
				.toList();
	}
}
