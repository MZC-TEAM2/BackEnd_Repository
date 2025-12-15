package com.mzc.backend.lms.domains.message.sse.controller;

import com.mzc.backend.lms.domains.message.sse.service.SseService;
import com.mzc.backend.lms.domains.message.sse.swagger.SseControllerSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController implements SseControllerSwagger {

    private final SseService sseService;

    @Override
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId) {
        return sseService.subscribe(userId);
    }
}
