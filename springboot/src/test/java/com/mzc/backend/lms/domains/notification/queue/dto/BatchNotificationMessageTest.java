package com.mzc.backend.lms.domains.notification.queue.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BatchNotificationMessage DTO 테스트
 */
@DisplayName("BatchNotificationMessage DTO 테스트")
class BatchNotificationMessageTest {

    @Test
    @DisplayName("강의 관련 배치 알림 생성")
    void createForCourse() {
        // given
        List<Long> recipientIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        // when
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                100L, 1, 10L, recipientIds, 20L, "과제 알림", "새 과제가 등록되었습니다."
        );

        // then
        assertThat(message.getBatchId()).isEqualTo(100L);
        assertThat(message.getTypeId()).isEqualTo(1);
        assertThat(message.getSenderId()).isEqualTo(10L);
        assertThat(message.getRecipientIds()).hasSize(5);
        assertThat(message.getCourseId()).isEqualTo(20L);
        assertThat(message.getTitle()).isEqualTo("과제 알림");
        assertThat(message.getMessage()).isEqualTo("새 과제가 등록되었습니다.");
        assertThat(message.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("수신자 수 반환")
    void getRecipientCount() {
        // given
        List<Long> recipientIds = Arrays.asList(1L, 2L, 3L);

        // when
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                100L, 1, 10L, recipientIds, 20L, "알림", "메시지"
        );

        // then
        assertThat(message.getRecipientCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("수신자 목록이 null인 경우 0 반환")
    void getRecipientCountWithNullList() {
        // when
        BatchNotificationMessage message = BatchNotificationMessage.builder()
                .batchId(100L)
                .typeId(1)
                .senderId(10L)
                .recipientIds(null)
                .message("메시지")
                .build();

        // then
        assertThat(message.getRecipientCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Builder를 사용한 배치 메시지 생성")
    void createWithBuilder() {
        // given
        List<Long> recipientIds = Arrays.asList(100L, 200L, 300L);

        // when
        BatchNotificationMessage message = BatchNotificationMessage.builder()
                .batchId(1L)
                .typeId(2)
                .senderId(50L)
                .recipientIds(recipientIds)
                .courseId(10L)
                .relatedEntityType("ASSIGNMENT")
                .relatedEntityId(30L)
                .title("과제 제출 마감")
                .message("과제 제출 마감일이 다가왔습니다.")
                .actionUrl("/courses/10/assignments/30")
                .build();

        // then
        assertThat(message.getBatchId()).isEqualTo(1L);
        assertThat(message.getTypeId()).isEqualTo(2);
        assertThat(message.getSenderId()).isEqualTo(50L);
        assertThat(message.getRecipientIds()).containsExactly(100L, 200L, 300L);
        assertThat(message.getCourseId()).isEqualTo(10L);
        assertThat(message.getRelatedEntityType()).isEqualTo("ASSIGNMENT");
        assertThat(message.getRelatedEntityId()).isEqualTo(30L);
        assertThat(message.getTitle()).isEqualTo("과제 제출 마감");
        assertThat(message.getMessage()).isEqualTo("과제 제출 마감일이 다가왔습니다.");
        assertThat(message.getActionUrl()).isEqualTo("/courses/10/assignments/30");
    }
}
