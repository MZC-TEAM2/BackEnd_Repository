package com.mzc.backend.lms.domains.message.sse.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE", description = "실시간 알림 API")
public interface SseControllerSwagger {

    @Operation(summary = "SSE 구독", description = "실시간 알림을 받기 위해 SSE 연결을 생성합니다.")
    SseEmitter subscribe(@Parameter(hidden = true) Long userId);
}
