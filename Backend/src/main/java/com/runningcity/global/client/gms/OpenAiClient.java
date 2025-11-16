package com.runningcity.global.client.gms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClient {
    @Value("${gms.api.key}")
    private String apiKey;

    @Value("${gms.api.base-url}")
    private String baseUrl;

    @Value("${gms.path.openai}")
    private String openaiPath;

    private final RestClient restClient = RestClient.create();

    private final ObjectMapper objectMapper;

    public String chat(String model, String system, String user) {
        ChatRequest body = new ChatRequest(
                model,
                List.of(
                        new Message("system", system),
                        new Message("user", user)
                ),
                0.7,
                4096
        );

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
            log.info("GMS 요청 JSON: {}", jsonBody);
        } catch (JsonProcessingException e) {
            log.error("GMS 요청 JSON 직렬화 실패", e);
            throw new IllegalStateException("Failed to serialize OpenAI request body", e);
        }

        String url = baseUrl + "/aa" + openaiPath;

        ChatResponse response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(jsonBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String errBody;

                    try {
                        if (res.getBody() != null) {
                            byte[] bytes = res.getBody().readAllBytes();
                            errBody = bytes.length > 0
                                    ? new String(bytes, StandardCharsets.UTF_8)
                                    : "(empty body)";
                        } else {
                            errBody = "(null body)";
                        }
                    } catch (Exception e) {
                        errBody = "(failed to read body: " + e.getMessage() + ")";
                    }

                    log.error("[OpenAiClient] HTTP {} from OpenAI/GMS. body={}",
                            res.getStatusCode(), errBody);

                    throw new RestClientException("OpenAI/GMS returned error: " +
                            res.getStatusCode() + ", body=" + errBody);
                })
                .body(ChatResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null) {
            log.error("Chat response is null");
            throw new IllegalStateException("Chat response is null");
        }

        return response.choices().get(0).message().content();
    }

    // DTO
    public record Message(String role, String content) {}

    public record ChatRequest(
            String model,
            List<Message> messages,
            Double temperature,
            Integer max_tokens
    ) {}

    public record ChatResponse(List<Choice> choices) {
        public record Choice(Message message) {}
    }
}
