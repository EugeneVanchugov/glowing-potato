package com.example.assassistant.service.openai;

import com.example.assassistant.domain.GPTFormattedResponse;
import com.example.assassistant.domain.OpenAIRequest;
import com.example.assassistant.domain.OpenAIResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * OpenAI API client.
 * <p>
 * It uses Spring WebClient to send requests to OpenAI API and receive responses.
 * It uses OpenAI API key from Configuration class to authenticate requests.
 * It uses OpenAI API endpoint from Configuration class to send requests.
 * It returns the string with the answer from GPT-3 model.
 */
@Slf4j
@AllArgsConstructor
public class OpenAIClient {
    private final WebClient webClient;
    private final PromptGenerator promptGenerator;
    private final ResponseParser responseParser;

    public Mono<GPTFormattedResponse> sendPrompt(String userInputMessage) {
        Objects.requireNonNull(userInputMessage, "User input message cannot be null");

        return webClient.post()
                .uri("/completions")
                .bodyValue(buildRequest(userInputMessage))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .doFirst(() -> log.info("Request to OpenAI API has been sent. User input message: {}", userInputMessage))
                .doOnNext(response -> log.info("Received response from OpenAI API. Response: {}", response))
                .doOnError(throwable -> log.error("Error while sending a request to OpenAI API", throwable))
                .map(responseParser::parseResponse);
    }

    private OpenAIRequest buildRequest(String userInputMessage) {
        return OpenAIRequest.builder()
                .model("text-davinci-003")
                .prompt(promptGenerator.buildPrompt(userInputMessage))
                .temperature(0.7)
                .max_tokens(1024)
                .top_p(1.0)
                .frequency_penalty(0.0)
                .presence_penalty(0.0)
                .build();
    }
}
