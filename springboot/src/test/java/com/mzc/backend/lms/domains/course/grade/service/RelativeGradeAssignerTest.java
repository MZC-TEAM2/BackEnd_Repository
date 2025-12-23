package com.mzc.backend.lms.domains.course.grade.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelativeGradeAssignerTest {
	
	@Test
	void quotaDenominatorUsesTotalPopulation_notEligibleCount() {
		// totalPopulation=100 (전체 수강생), but only 80 are eligible for relative grading
		// (e.g., 20 students are forced-F due to attendance and removed from ranking)
		int totalPopulation = 100;
		int eligible = 80;
		
		List<RelativeGradeAssigner.ScoreRow> rows = new ArrayList<>(eligible);
		for (int i = 0; i < eligible; i++) {
			long studentId = i + 1L;
			BigDecimal score = BigDecimal.valueOf(100_000L - i); // strictly decreasing, no ties
			rows.add(new RelativeGradeAssigner.ScoreRow(studentId, score));
		}
		
		Map<Long, String> assigned = RelativeGradeAssigner.assign(rows, totalPopulation);
		
		// If denominator were "eligible(80)" then floor(80*0.10)=8 (A+ 8명).
		// We expect denominator to be totalPopulation(100) so A+ should be 10명.
		long aPlusCount = assigned.values().stream().filter("A+"::equals).count();
		assertEquals(10, aPlusCount);
		
		// Top scorer must get A+
		assertEquals("A+", assigned.get(1L));
	}
}


