package com.mzc.backend.lms.domains.assessment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.backend.lms.domains.assessment.dto.request.AssessmentCreateRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.request.AssessmentUpdateRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.request.AttemptGradeRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.request.AttemptSubmitRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.response.*;
import com.mzc.backend.lms.domains.assessment.entity.Assessment;
import com.mzc.backend.lms.domains.assessment.entity.AssessmentAttempt;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import com.mzc.backend.lms.domains.assessment.repository.AssessmentAttemptRepository;
import com.mzc.backend.lms.domains.assessment.repository.AssessmentRepository;
import com.mzc.backend.lms.domains.assessment.util.QuestionDataMasker;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class AssessmentService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration LATE_GRACE_PERIOD = Duration.ofMinutes(10);
    private static final BigDecimal LATE_PENALTY_RATE = new BigDecimal("0.10");

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;

    private final PostRepository postRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserViewService userViewService;

    // ---------------------------
    // 교수
    // ---------------------------

    public List<AssessmentListItemResponseDto> listForProfessor(Long courseId, AssessmentType type, long professorId) {
        Objects.requireNonNull(courseId, "courseId");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        boolean isProfessor = course.getProfessor().getProfessorId().equals(professorId);
        if (!isProfessor) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        // 교수는 시작 전 포함 전체 조회 가능
        return assessmentRepository.findActiveByCourse(courseId, type).stream()
                .map(AssessmentListItemResponseDto::from)
                .toList();
    }

    public AssessmentDetailResponseDto getDetailForProfessor(Long assessmentId, long professorId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        boolean isProfessor = course.getProfessor().getProfessorId().equals(professorId);
        if (!isProfessor) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        // 교수에게는 정답 포함 원본 제공
        return AssessmentDetailResponseDto.from(assessment, assessment.getQuestionData());
    }

    /**
     * 응시자/응시 결과 목록 조회 (교수)
     * - 명세서 6.10
     */
    public List<ProfessorAttemptListItemResponseDto> listAttemptsForProfessor(Long assessmentId, String status, long professorId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        String normalized = normalizeAttemptStatus(status);
        List<AssessmentAttempt> attempts = attemptRepository.findActiveByAssessmentIdAndStatus(assessmentId, normalized);

        // userId -> name 배치 조회 (복호화 포함, N+1 방지)
        List<String> userIds = attempts.stream()
                .map(AssessmentAttempt::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .map(String::valueOf)
                .toList();
        Map<String, String> nameMap = userViewService.getUserNames(userIds);

        List<ProfessorAttemptListItemResponseDto> result = new ArrayList<>(attempts.size());
        for (AssessmentAttempt at : attempts) {
            Long uid = at.getUserId();
            result.add(ProfessorAttemptListItemResponseDto.builder()
                    .attemptId(at.getId())
                    .examId(assessment.getId())
                    .courseId(assessment.getCourseId())
                    .student(ProfessorAttemptListItemResponseDto.StudentInfo.builder()
                            .id(uid)
                            .studentNumber(uid != null ? uid.toString() : null)
                            .name(uid != null ? nameMap.get(uid.toString()) : null)
                            .build())
                    .startedAt(at.getStartedAt())
                    .submittedAt(at.getSubmittedAt())
                    .isLate(at.getIsLate())
                    .latePenaltyRate(at.getLatePenaltyRate())
                    .score(at.getScore())
                    .feedback(at.getFeedback())
                    .build());
        }
        return result;
    }

    /**
     * 응시 결과 상세 조회(답안 포함) (교수)
     * - 명세서 6.11
     */
    public ProfessorAttemptDetailResponseDto getAttemptDetailForProfessor(Long attemptId, long professorId) {
        AssessmentAttempt attempt = attemptRepository.findActiveWithAssessment(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("응시 정보를 찾을 수 없습니다."));

        Assessment assessment = attempt.getAssessment();
        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        Long uid = attempt.getUserId();
        String name = (uid != null) ? userViewService.getUserName(uid.toString()) : null;

        // answerData는 DB에는 answers 객체만 저장되어 있으므로, 명세서 형식에 맞게 {answers: ...}로 래핑
        JsonNode answersNode = readJsonOrNull(attempt.getAnswerData());
        com.fasterxml.jackson.databind.node.ObjectNode answerWrapper = MAPPER.createObjectNode();
        if (answersNode != null) {
            answerWrapper.set("answers", answersNode);
        } else {
            answerWrapper.putNull("answers");
        }

        JsonNode questionNode = readJsonOrNull(assessment.getQuestionData());

        return ProfessorAttemptDetailResponseDto.builder()
                .attemptId(attempt.getId())
                .examId(assessment.getId())
                .courseId(assessment.getCourseId())
                .student(ProfessorAttemptListItemResponseDto.StudentInfo.builder()
                        .id(uid)
                        .studentNumber(uid != null ? uid.toString() : null)
                        .name(name)
                        .build())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .isLate(attempt.getIsLate())
                .latePenaltyRate(attempt.getLatePenaltyRate())
                .score(attempt.getScore())
                .feedback(attempt.getFeedback())
                .answerData(answerWrapper)
                .questionData(questionNode)
                .build();
    }

    @Transactional
    public AssessmentDetailResponseDto create(BoardType boardType, AssessmentCreateRequestDto req, long professorId) {
        Long courseId = Objects.requireNonNull(req.getCourseId(), "courseId");

        // boardType 제한 (다른 게시판 타입으로 유입되는 실수 방지)
        if (boardType != BoardType.QUIZ && boardType != BoardType.EXAM) {
            throw new IllegalArgumentException("QUIZ/EXAM 게시판만 허용됩니다.");
        }
        // boardType - type 정합성 보장
        if (boardType == BoardType.QUIZ && req.getType() != AssessmentType.QUIZ) {
            throw new IllegalArgumentException("QUIZ 게시판에는 type=QUIZ만 등록할 수 있습니다.");
        }
        if (boardType == BoardType.EXAM && req.getType() == AssessmentType.QUIZ) {
            throw new IllegalArgumentException("EXAM 게시판에는 QUIZ 유형을 등록할 수 없습니다.");
        }
        validateQuestionData(req.getType(), req.getQuestionData());

        // 1) 강의/권한 검증
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        boolean isProfessor = course.getProfessor().getProfessorId().equals(professorId);
        if (!isProfessor) {
            throw new IllegalArgumentException("퀴즈/시험 생성 권한이 없습니다.");
        }

        // 2) 게시판 카테고리 조회
        BoardCategory category = boardCategoryRepository.findByBoardType(boardType)
                .orElseThrow(() -> new IllegalArgumentException("게시판 카테고리를 찾을 수 없습니다."));

        // 3) Post 생성 (제목/설명)
        PostType postType = (boardType == BoardType.QUIZ) ? PostType.QUIZ : PostType.EXAM;
        Post post = Post.builder()
                .category(category)
                .title(req.getTitle())
                .content(req.getContent())
                .authorId(professorId)
                .postType(postType)
                .isAnonymous(false)
                .courseId(courseId)
                .build();
        Post savedPost = Objects.requireNonNull(postRepository.save(post));

        // 4) Assessment 생성 (exams)
        Assessment assessment = Assessment.builder()
                .post(savedPost)
                .courseId(courseId)
                .type(req.getType())
                .startAt(req.getStartAt())
                .durationMinutes(req.getDurationMinutes())
                .totalScore(req.getTotalScore())
                .isOnline(req.getIsOnline())
                .location(req.getLocation())
                .instructions(req.getInstructions())
                .questionCount(req.getQuestionCount())
                .passingScore(req.getPassingScore())
                .questionData(writeJson(req.getQuestionData()))
                .createdBy(professorId)
                .build();
        Assessment saved = Objects.requireNonNull(assessmentRepository.save(assessment));

        return AssessmentDetailResponseDto.from(saved, saved.getQuestionData());
    }

    @Transactional
    public AssessmentDetailResponseDto update(Long assessmentId, AssessmentUpdateRequestDto req, long professorId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        boolean isProfessor = course.getProfessor().getProfessorId().equals(professorId);
        if (!isProfessor) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // Post 수정(제목/설명)
        Post post = assessment.getPost();
        if (req.getTitle() != null || req.getContent() != null) {
            post.update(
                    req.getTitle() != null ? req.getTitle() : post.getTitle(),
                    req.getContent() != null ? req.getContent() : post.getContent(),
                    post.isAnonymous()
            );
        }

        // Assessment 수정
        assessment.update(
                req.getStartAt(),
                req.getDurationMinutes(),
                req.getTotalScore(),
                req.getIsOnline(),
                req.getLocation(),
                req.getInstructions(),
                req.getQuestionCount(),
                req.getPassingScore(),
                req.getQuestionData() != null ? writeJson(req.getQuestionData()) : null
        );
        if (req.getQuestionData() != null) {
            validateQuestionData(assessment.getType(), req.getQuestionData());
        }

        return AssessmentDetailResponseDto.from(assessment, assessment.getQuestionData());
    }

    @Transactional
    public void delete(Long assessmentId, long professorId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        boolean isProfessor = course.getProfessor().getProfessorId().equals(professorId);
        if (!isProfessor) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        assessment.delete();
        assessment.getPost().delete();
    }

    // ---------------------------
    // 학생
    // ---------------------------

    public List<AssessmentListItemResponseDto> listForStudent(Long courseId, AssessmentType type, long studentId) {
        Objects.requireNonNull(courseId, "courseId");
        // 수강 중인지 확인
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("수강 중인 강의만 조회할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        return assessmentRepository.findVisibleByCourseForStudent(courseId, type, now).stream()
                .map(AssessmentListItemResponseDto::from)
                .toList();
    }

    public AssessmentDetailResponseDto getDetailForStudent(Long assessmentId, long studentId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        // 수강 중인지 확인
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, assessment.getCourseId())) {
            throw new IllegalArgumentException("수강 중인 강의만 조회할 수 있습니다.");
        }

        // 시작 전이면 숨김
        if (LocalDateTime.now().isBefore(assessment.getStartAt())) {
            throw new IllegalArgumentException("시작 전 퀴즈/시험은 조회할 수 없습니다.");
        }

        String masked = QuestionDataMasker.maskCorrectAnswers(assessment.getQuestionData());
        return AssessmentDetailResponseDto.from(assessment, masked);
    }

    @Transactional
    public AttemptStartResponseDto startAttempt(Long assessmentId, long studentId) {
        Assessment assessment = assessmentRepository.findActiveWithPost(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈/시험을 찾을 수 없습니다."));

        // 수강 중인지 확인
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, assessment.getCourseId())) {
            throw new IllegalArgumentException("수강 중인 강의만 응시할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(assessment.getStartAt())) {
            throw new IllegalArgumentException("시작 전에는 응시를 시작할 수 없습니다.");
        }

        AssessmentAttempt attempt = attemptRepository.findActiveByAssessmentIdAndUserId(assessmentId, studentId)
                .orElse(null);
        if (attempt == null) {
            // 시작/종료 시간이 별도 컬럼이 없으므로 startAt + durationMinutes를 종료로 간주
            // (단, 이미 attempt가 있는 사용자는 재진입을 허용해야 해서 신규 생성 시점에만 차단)
            if (now.isAfter(assessment.endAt())) {
                throw new IllegalArgumentException("종료된 퀴즈/시험입니다.");
            }
            try {
                attempt = Objects.requireNonNull(attemptRepository.save(
                        AssessmentAttempt.builder()
                                .assessment(assessment)
                                .userId(studentId)
                                .build()
                ));
            } catch (DataIntegrityViolationException e) {
                // 동시 요청으로 UNIQUE(exam_id, user_id) 충돌 시 재조회로 복구
                attempt = attemptRepository.findActiveByAssessmentIdAndUserId(assessmentId, studentId)
                        .orElseThrow(() -> new IllegalArgumentException("응시 시작 처리에 실패했습니다."));
            }
        }

        if (attempt.isSubmitted()) {
            throw new IllegalArgumentException("이미 제출한 퀴즈/시험입니다.");
        }

        if (attempt.getStartedAt() == null) {
            attempt.start(now);
        }

        LocalDateTime endAt = attempt.getStartedAt().plusMinutes(assessment.getDurationMinutes());
        long remainingSeconds = Math.max(0, Duration.between(now, endAt).getSeconds());

        return AttemptStartResponseDto.builder()
                .attemptId(attempt.getId())
                .startedAt(attempt.getStartedAt())
                .endAt(endAt)
                .remainingSeconds(remainingSeconds)
                .build();
    }

    @Transactional
    public AttemptSubmitResponseDto submitAttempt(Long attemptId, AttemptSubmitRequestDto req, long studentId) {
        AssessmentAttempt attempt = attemptRepository.findActiveWithAssessment(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("응시 정보를 찾을 수 없습니다."));

        if (!attempt.getUserId().equals(studentId)) {
            throw new IllegalArgumentException("본인 응시만 제출할 수 있습니다.");
        }
        if (attempt.isSubmitted()) {
            throw new IllegalArgumentException("이미 제출한 퀴즈/시험입니다.");
        }
        if (attempt.getStartedAt() == null) {
            throw new IllegalArgumentException("응시 시작 후 제출할 수 있습니다.");
        }

        Assessment assessment = attempt.getAssessment();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = attempt.getStartedAt().plusMinutes(assessment.getDurationMinutes());
        LocalDateTime graceDeadline = deadline.plusSeconds(LATE_GRACE_PERIOD.getSeconds());
        if (now.isAfter(graceDeadline)) {
            throw new IllegalArgumentException("제출 가능 시간이 지났습니다. (마감 후 10분 초과)");
        }
        boolean isLate = now.isAfter(deadline);

        // late 정책: 마감 후 10분까지는 10% 감점
        BigDecimal penaltyRate = isLate ? LATE_PENALTY_RATE : BigDecimal.ZERO;
        if (isLate) {
            attempt.markLate(BigDecimal.ZERO, penaltyRate);
        } else {
            attempt.markOnTime();
        }

        BigDecimal score;
        if (assessment.getType() == AssessmentType.QUIZ) {
            // 퀴즈: 객관식만 + 제출 즉시 자동채점
            score = gradeQuiz(assessment, req.getAnswers());
            // late면 점수에 비율 감점 적용
            score = applyRatePenalty(score, penaltyRate);
        } else {
            // 시험: 제출만 저장, 교수 채점에서 점수 반영
            score = null;
        }

        attempt.submit(writeJson(req.getAnswers()), now, score);

        return AttemptSubmitResponseDto.builder()
                .attemptId(attempt.getId())
                .submittedAt(now)
                .isLate(isLate)
                .latePenaltyRate(penaltyRate)
                .score(score)
                .build();
    }

    /**
     * 시험 채점 (교수)
     * - 시험은 주관식 가능 → 제출 즉시 자동채점 X
     * - 교수는 원점수(raw)만 보내고, 서버에서 latePenaltyRate만큼 감점 적용 후 저장
     */
    @Transactional
    public AttemptGradeResponseDto gradeAttempt(Long attemptId, AttemptGradeRequestDto req, long professorId) {
        AssessmentAttempt attempt = attemptRepository.findActiveWithAssessment(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("응시 정보를 찾을 수 없습니다."));

        Assessment assessment = attempt.getAssessment();
        if (assessment.getType() == AssessmentType.QUIZ) {
            throw new IllegalArgumentException("퀴즈는 자동채점 대상입니다.");
        }
        if (!attempt.isSubmitted()) {
            throw new IllegalArgumentException("제출된 응시만 채점할 수 있습니다.");
        }

        Course course = courseRepository.findById(assessment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("채점 권한이 없습니다.");
        }

        BigDecimal rawScore = req.getScore();
        BigDecimal finalScore = applyRatePenalty(rawScore, attempt.getLatePenaltyRate());
        attempt.grade(finalScore, req.getFeedback(), professorId);

        return AttemptGradeResponseDto.builder()
                .attemptId(attempt.getId())
                .score(attempt.getScore())
                .isLate(attempt.getIsLate())
                .latePenaltyRate(attempt.getLatePenaltyRate())
                .gradedAt(attempt.getGradedAt())
                .gradedBy(attempt.getGradedBy())
                .build();
    }

    private String writeJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 직렬화에 실패했습니다.");
        }
    }

    /**
     * 문제 JSON 최소 스키마 검증
     * - 복수정답은 고려하지 않음(단일 정답)
     * - 객관식(MCQ)은 correctChoiceIndex(0-based)로 정답 관리
     */
    private void validateQuestionData(AssessmentType assessmentType, JsonNode questionData) {
        if (questionData == null || questionData.isNull()) {
            throw new IllegalArgumentException("문제 JSON(questionData)은 필수입니다.");
        }
        JsonNode questions = questionData.get("questions");
        if (questions == null || !questions.isArray()) {
            throw new IllegalArgumentException("questionData.questions는 배열이어야 합니다.");
        }

        for (JsonNode q : questions) {
            if (q == null || !q.isObject()) continue;

            JsonNode type = q.get("type");
            String typeStr = (type != null && type.isTextual()) ? type.asText() : null;
            if (assessmentType == AssessmentType.QUIZ) {
                // 퀴즈는 객관식만 허용
                if (!"MCQ".equalsIgnoreCase(typeStr)) {
                    throw new IllegalArgumentException("퀴즈는 객관식(MCQ) 문항만 허용합니다.");
                }
            }

            if ("MCQ".equalsIgnoreCase(typeStr)) {
                JsonNode choices = q.get("choices");
                if (choices == null || !choices.isArray() || choices.size() == 0) {
                    throw new IllegalArgumentException("MCQ 문항은 choices 배열이 필요합니다.");
                }
                JsonNode idx = q.get("correctChoiceIndex");
                if (idx == null || !idx.canConvertToInt()) {
                    throw new IllegalArgumentException("MCQ 문항은 correctChoiceIndex(숫자, 0-based)가 필요합니다.");
                }
                int i = idx.asInt();
                if (i < 0 || i >= choices.size()) {
                    throw new IllegalArgumentException("correctChoiceIndex 범위가 choices 크기를 벗어났습니다.");
                }
            }
        }
    }

    private BigDecimal gradeQuiz(Assessment assessment, JsonNode answers) {
        if (assessment.getQuestionData() == null || assessment.getQuestionData().isBlank()) {
            throw new IllegalArgumentException("퀴즈 문제 데이터가 없습니다.");
        }
        try {
            JsonNode qd = MAPPER.readTree(assessment.getQuestionData());
            JsonNode questions = qd.get("questions");
            if (questions == null || !questions.isArray()) {
                throw new IllegalArgumentException("퀴즈 문제 데이터 형식이 올바르지 않습니다.");
            }

            BigDecimal total = BigDecimal.ZERO;
            for (JsonNode q : questions) {
                if (q == null || !q.isObject()) continue;
                String qid = q.hasNonNull("id") ? q.get("id").asText() : null;
                String type = q.hasNonNull("type") ? q.get("type").asText() : null;
                if (qid == null || type == null) continue;

                if (!"MCQ".equalsIgnoreCase(type)) {
                    // 퀴즈는 MCQ만 허용인데, 방어적으로 무시
                    continue;
                }
                int correct = q.get("correctChoiceIndex").asInt();

                BigDecimal points = BigDecimal.ZERO;
                if (q.has("points") && q.get("points").isNumber()) {
                    points = q.get("points").decimalValue();
                }

                JsonNode ansNode = (answers != null) ? answers.get(qid) : null;
                if (ansNode != null && ansNode.canConvertToInt()) {
                    int chosen = ansNode.asInt();
                    if (chosen == correct) {
                        total = total.add(points);
                    }
                }
            }
            return total;
        } catch (Exception e) {
            throw new IllegalArgumentException("퀴즈 채점에 실패했습니다.");
        }
    }

    private BigDecimal applyRatePenalty(BigDecimal score, BigDecimal rate) {
        if (score == null) return null;
        if (rate == null) return score;
        if (rate.compareTo(BigDecimal.ZERO) <= 0) return score;
        // score * (1 - rate)
        BigDecimal multiplier = BigDecimal.ONE.subtract(rate);
        BigDecimal result = score.multiply(multiplier);
        // 소수 2자리(테이블 scale=2)에 맞춰 반올림
        return result.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String normalizeAttemptStatus(String status) {
        String s = (status == null || status.isBlank()) ? "ALL" : status.trim().toUpperCase();
        return switch (s) {
            case "ALL", "SUBMITTED", "IN_PROGRESS" -> s;
            default -> throw new IllegalArgumentException("status는 ALL|SUBMITTED|IN_PROGRESS 만 허용합니다.");
        };
    }

    private JsonNode readJsonOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return MAPPER.readTree(raw);
        } catch (Exception e) {
            return null;
        }
    }
}


