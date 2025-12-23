package com.mzc.backend.lms.domains.academy.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.course.course.dto.AcademicTermDto;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfessorAcademicTermService {
	
	private final CourseRepository courseRepository;
	
	@Transactional(readOnly = true)
	public List<AcademicTermDto> listMyAcademicTerms(Long professorId) {
		Objects.requireNonNull(professorId, "professorId");
		
		List<AcademicTerm> terms = courseRepository.findDistinctAcademicTermsByProfessorId(professorId);
		return terms.stream()
				.sorted(Comparator.comparing(AcademicTerm::getId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.map(t -> AcademicTermDto.builder()
						.id(t.getId())
						.year(t.getYear())
						.termType(t.getTermType())
						.startDate(t.getStartDate())
						.endDate(t.getEndDate())
						.build())
				.toList();
	}
}


