package com.example.assassistant.service.openai;

import com.example.assassistant.domain.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final WebClient openAiApi;
    private final PromptGenerator promptGenerator;
    private final ResponseParser responseParser;

    public Mono<GPTFormattedResponse> sendPrompt(String userInputMessage) {
        Objects.requireNonNull(userInputMessage, "User input message cannot be null");

        return sendRequest(
                openAiApi.post().uri("/completions"),
                buildPromptRequest(userInputMessage),
                OpenAIResponse.class
        ).map(responseParser::parseResponse);
    }

    public Mono<String> generateImage(String prompt) {
        Objects.requireNonNull(prompt, "Prompt cannot be null");

        return sendRequest(
                openAiApi.post().uri("/images/generations"),
                buildImageGenerationRequest(prompt),
                ImageGenerationResponse.class
        ).map(it -> it.data().get(0).url());
    }

    private ImageGenerationRequest buildImageGenerationRequest(String prompt) {
        return ImageGenerationRequest.builder()
                .prompt(prompt)
                .n(1)
                .size("256x256")
                .build();
    }

    private OpenAIRequest buildPromptRequest(String userInputMessage) {
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

    private <T, V> Mono<T> sendRequest(
            WebClient.RequestBodySpec responseSpec,
            V body,
            Class<T> clazz
    ) {
        return responseSpec
                .bodyValue(body)
                .retrieve()
                .bodyToMono(clazz)
                .doFirst(() -> log.info("Request to OpenAI API has been sent. Request: {}", body))
                .doOnNext(response -> log.info("Received response from OpenAI API. Response: {}", response))
                .doOnError(throwable -> log.error("Error while sending a request to OpenAI API", throwable));
    }
}
