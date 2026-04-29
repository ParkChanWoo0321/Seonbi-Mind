package youngju.seonbimind.classic.gpt.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import youngju.seonbimind.classic.gpt.dto.MeaningEvaluationResult;

@Service
@RequiredArgsConstructor
public class GptMeaningEvaluationService {

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    @Value("${openai.responses-url:https://api.openai.com/v1/responses}")
    private String responsesUrl;

    public MeaningEvaluationResult evaluate(String correctMeaning, String userAnswer) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "OpenAI API key is not configured. Set OPENAI_API_KEY or openai.api-key before running the server."
            );
        }

        Map<String, Object> request = createRequest(correctMeaning, userAnswer);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(responsesUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RESPONSE_TYPE);

            return parseEvaluationResult(response);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "OpenAI API request failed. status=" + exception.getStatusCode() + ", body=" + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RestClientException | JacksonException | ClassCastException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GPT meaning evaluation failed.", exception);
        }
    }

    private Map<String, Object> createRequest(String correctMeaning, String userAnswer) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("instructions", """
                You grade whether two Korean explanations have the same meaning.
                Return only the requested JSON object.
                Use correct=true when the user answer preserves the same meaning, even if wording differs.
                The reason field must be written in Korean in the tone of a kind traditional village teacher, called Hunjangnim.
                Keep the reason concise, warm, and instructive.
                For a correct answer, praise the learner gently and explain why the meaning matches.
                For an incorrect answer, point out the difference gently and encourage another attempt.
                Do not use difficult archaic words excessively.
                """);
        request.put("input", "Correct meaning: " + correctMeaning + "\nUser answer: " + userAnswer);
        request.put("text", Map.of(
                "format", Map.of(
                        "type", "json_schema",
                        "name", "meaning_evaluation",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "correct", Map.of("type", "boolean"),
                                        "reason", Map.of("type", "string")
                                ),
                                "required", List.of("correct", "reason"),
                                "additionalProperties", false
                        )
                )
        ));
        return request;
    }

    @SuppressWarnings("unchecked")
    private MeaningEvaluationResult parseEvaluationResult(Map<String, Object> response) throws JacksonException {
        if (response == null) {
            throw new IllegalArgumentException("GPT response is empty.");
        }

        String outputText = findOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalArgumentException("GPT response has no output text.");
        }

        return objectMapper.readValue(outputText, MeaningEvaluationResult.class);
    }

    @SuppressWarnings("unchecked")
    private String findOutputText(Map<String, Object> response) {
        Object directOutputText = response.get("output_text");
        if (directOutputText instanceof String outputText) {
            return outputText;
        }

        Object output = response.get("output");
        if (!(output instanceof List<?> outputItems)) {
            return null;
        }

        for (Object outputItem : outputItems) {
            if (!(outputItem instanceof Map<?, ?> item)) {
                continue;
            }

            Object content = item.get("content");
            if (!(content instanceof List<?> contentItems)) {
                continue;
            }

            for (Object contentItem : contentItems) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object text = contentMap.get("text");
                if (text instanceof String outputText) {
                    return outputText;
                }
            }
        }

        return null;
    }
}
