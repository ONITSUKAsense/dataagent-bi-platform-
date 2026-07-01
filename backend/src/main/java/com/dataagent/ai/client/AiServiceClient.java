package com.dataagent.ai.client;

import com.dataagent.ai.dto.AgentRunRequest;
import com.dataagent.ai.dto.AgentRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public AgentRunResult runAgent(AgentRunRequest request) {
        String url = aiServiceUrl + "/api/agent/run";
        return restTemplate.postForObject(url, request, AgentRunResult.class);
    }
}
