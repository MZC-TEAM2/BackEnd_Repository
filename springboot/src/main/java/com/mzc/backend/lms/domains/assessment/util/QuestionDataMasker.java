package com.mzc.backend.lms.domains.assessment.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 학생 응답에서 정답 필드 제거(정답 유출 방지)
 */
public final class QuestionDataMasker {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private QuestionDataMasker() {
    }

    public static String maskCorrectAnswers(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return rawJson;
        try {
            JsonNode root = MAPPER.readTree(rawJson);
            strip(root);
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            // JSON 파싱 실패 시 원본을 그대로 내려주면 정답(correctAnswer) 유출 가능성이 있으므로
            // 학생 응답에서는 안전하게 null 처리
            return null;
        }
    }

    private static void strip(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.remove("correctAnswer");
            obj.remove("correctChoiceIndex");
            obj.properties().forEach(e -> strip(e.getValue()));
        } else if (node.isArray()) {
            for (JsonNode child : node) strip(child);
        }
    }
}


