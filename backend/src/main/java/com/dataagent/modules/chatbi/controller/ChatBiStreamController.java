package com.dataagent.modules.chatbi.controller;

import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.chatbi.dto.AskRequest;
import com.dataagent.modules.chatbi.service.ChatBiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chatbi")
@RequiredArgsConstructor
@Tag(name = "ChatBI 智能分析")
public class ChatBiStreamController {

    private final ChatBiService chatBiService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "提问（SSE 流式）")
    public SseEmitter askStream(@Valid @RequestBody AskRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        SseEmitter emitter = new SseEmitter(300_000L); // 5min timeout

        executor.execute(() -> {
            try {
                Map<String, Object> result = chatBiService.ask(request, userId);

                // Send events step by step
                if (result.containsKey("sql")) {
                    emitter.send(SseEmitter.event()
                            .name("sql")
                            .data(result.get("sql") != null ? result.get("sql").toString() : ""));
                }

                if (result.containsKey("chartOption") && result.get("chartOption") != null) {
                    emitter.send(SseEmitter.event()
                            .name("chart")
                            .data(result.get("chartOption")));
                }

                emitter.send(SseEmitter.event()
                        .name("answer")
                        .data(result.get("answer") != null ? result.get("answer").toString() : ""));

                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(result));

                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(e.getMessage()));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
