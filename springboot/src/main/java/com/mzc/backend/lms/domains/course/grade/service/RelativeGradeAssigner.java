package com.mzc.backend.lms.domains.course.grade.service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 상대평가 등급 배정 유틸
 *
 * <p>정책:</p>
 * <ul>
 *   <li>등급 정원(비율)은 totalPopulation(전체 수강생 수 등)을 분모로 계산</li>
 *   <li>실제 배정(랭킹)은 rows(대상 집단)만 대상으로 수행</li>
 *   <li>경계 동점이면 해당 점수대는 상위 등급을 주지 않고 한 단계 아래로 내림</li>
 *   <li>같은 점수는 가장 낮은 등급으로 통일(강제 정규화)</li>
 * </ul>
 */
final class RelativeGradeAssigner {

    private RelativeGradeAssigner() {}

    record ScoreRow(Long studentId, BigDecimal finalScore) {}

    private record GradeBucket(String label, double ratio) {}

    static Map<Long, String> assign(List<ScoreRow> rows, int totalPopulation) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        int n = rows.size();
        Map<Long, String> out = new HashMap<>(Math.max(16, n * 2));

        // Defensive: if caller passes 0/negative, fall back to "rows.size()" so we still grade the given population.
        int denom = (totalPopulation > 0) ? totalPopulation : n;

        List<ScoreRow> sorted = rows.stream()
                .sorted(Comparator
                        .comparing(ScoreRow::finalScore, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(ScoreRow::studentId))
                .toList();

        List<GradeBucket> buckets = List.of(
                new GradeBucket("A+", 0.10),
                new GradeBucket("A0", 0.15),
                new GradeBucket("A-", 0.05),
                new GradeBucket("B+", 0.10),
                new GradeBucket("B0", 0.20),
                new GradeBucket("B-", 0.10),
                new GradeBucket("C+", 0.05),
                new GradeBucket("C0", 0.10),
                new GradeBucket("C-", 0.05),
                new GradeBucket("D+", 0.03),
                new GradeBucket("D0", 0.04),
                new GradeBucket("D-", 0.03)
        );

        int idx = 0;
        for (int bi = 0; bi < buckets.size(); bi++) {
            GradeBucket b = buckets.get(bi);
            String currentLabel = b.label();
            String nextLabel = (bi + 1 < buckets.size()) ? buckets.get(bi + 1).label() : "F";

            int cnt = (int) Math.floor(denom * b.ratio());
            int startIdx = idx;
            for (int k = 0; k < cnt && idx < n; k++, idx++) {
                out.put(sorted.get(idx).studentId(), currentLabel);
            }

            // 등급 경계 동점 처리:
            // 현재 버킷의 마지막 점수 == 다음 점수이면, 그 점수대(현재 버킷에 들어간 사람들)는 nextLabel로 내림
            if (idx < n && idx > startIdx) {
                BigDecimal lastScore = sorted.get(idx - 1).finalScore();
                BigDecimal nextScore = sorted.get(idx).finalScore();
                if (lastScore != null && nextScore != null && lastScore.compareTo(nextScore) == 0) {
                    BigDecimal tieScore = lastScore;
                    for (int j = idx - 1; j >= startIdx; j--) {
                        BigDecimal s = sorted.get(j).finalScore();
                        if (s == null || s.compareTo(tieScore) != 0) {
                            break;
                        }
                        out.put(sorted.get(j).studentId(), nextLabel);
                    }
                }
            }
        }

        // 남은 인원은 F
        while (idx < n) {
            out.put(sorted.get(idx).studentId(), "F");
            idx++;
        }

        // 동점 처리(강제 정규화):
        // 같은 finalScore를 가진 학생들은 "가장 낮은 등급"으로 통일
        // (경계 동점에서 상위 등급을 주지 않는 정책을 일반화)
        Map<BigDecimal, String> worstByScore = new HashMap<>();
        Map<String, Integer> rank = gradeRank();
        for (ScoreRow s : sorted) {
            BigDecimal score = s.finalScore();
            String g = out.get(s.studentId());
            if (g == null) continue;
            String prev = worstByScore.get(score);
            if (prev == null) {
                worstByScore.put(score, g);
            } else {
                int prevRank = rank.getOrDefault(prev, 999);
                int curRank = rank.getOrDefault(g, 999);
                if (curRank > prevRank) {
                    worstByScore.put(score, g);
                }
            }
        }
        for (ScoreRow s : sorted) {
            BigDecimal score = s.finalScore();
            String worst = worstByScore.get(score);
            if (worst != null) {
                out.put(s.studentId(), worst);
            }
        }

        return out;
    }

    private static Map<String, Integer> gradeRank() {
        // 낮을수록 "좋은 등급"
        Map<String, Integer> r = new HashMap<>();
        String[] order = {"A+", "A0", "A-", "B+", "B0", "B-", "C+", "C0", "C-", "D+", "D0", "D-", "F"};
        for (int i = 0; i < order.length; i++) {
            r.put(order[i], i);
        }
        return r;
    }
}


