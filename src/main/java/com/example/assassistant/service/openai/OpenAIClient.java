package com.example.assassistant.service.openai;

import com.example.assassistant.config.Configuration;
import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.domain.OpenAIRequest;
import com.example.assassistant.domain.OpenAIResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
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
@AllArgsConstructor
public class OpenAIClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAIClient.class);

    private final WebClient webClient;
    private final ConversationLog conversationLog;

    public Mono<OpenAIResponse> sendPrompt(String userInputMessage) {
        Objects.requireNonNull(userInputMessage, "User input message cannot be null");

        log.info("Sending request to OpenAI API. User input message: {}", userInputMessage);

        return webClient.post()
                .uri("/completions")
                .header("Authorization", "Bearer " + Configuration.OPENAI_API_KEY)
                .bodyValue(
                        OpenAIRequest.builder()
                                .model("text-davinci-003")
                                .prompt(buildPrompt(userInputMessage))
                                .temperature(0.7)
                                .max_tokens(1024)
                                .top_p(1.0)
                                .frequency_penalty(0.0)
                                .presence_penalty(0.0)
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .doOnError(throwable -> log.error("Error while sending a request to OpenAI API", throwable));
    }

    /**
     * Builds the prompt for OpenAI GPT-3 model.
     * <p>
     * The prompt consists of the following parts:
     * 1. The previous context of the conversation.
     * 2. Rules to generate a specific GPT-3 answer format.
     * 4. The user input message.
     *
     * @param userInputMessage the user input message.
     * @return the prompt for OpenAI GPT-3 model.
     */
    @NotNull
    private String buildPrompt(String userInputMessage) {
        StringBuilder prompt = new StringBuilder();


        /**
         * This is the prompt to generate a specific GPT-3 answer format.
         * <p>
         * Every GPT-3 response has to have the following format:
         * {
         *   "answer": <GPT-3 model answer>,
         *   "context": {
         *     "action": <action name to perform as a Virtual Assistant>,
         *     "parameters": { <parameters for the action> }
         *   }
         * }
         * <p>
         *     The action - is the one of the following:
         *     1. "GenerateImage" - to generate an image.
         * <p>
         *     Action "GenerateImage" has the following parameters:
         *     1. "prompt" - the prompt for the image generation.
         * <p>
         *     If the action is unknown, the Virtual Assistant should just return the following response:
         *     {
         *         "answer": <GPT-3 model answer>
         *     }
         */

        prompt.append("Every your response has to have the following format:\n");
        prompt.append("""
                {
                    "answer": <GPT-3 model answer>,
                    "context": {
                        "action": <action name to perform as a Virtual Assistant>,
                        "parameters": { <parameters for the action> }
                    }
                }
                """);
        prompt.append("The action - is the one of the following:\n");
        prompt.append("1. `GenerateImage` - to generate an image.\n");

        prompt.append("Action `GenerateImage` has the following parameters:\n");
        prompt.append("1. `prompt` - the prompt for the image generation.\n");

        prompt.append("If the action is unknown, the you should just return the following response:\n");
        prompt.append("""
                {
                    "answer": <GPT-3 model answer>
                }
                """);

        prompt.append("I want you to remember the context of our conversation.\n");
        prompt.append("I will send you the history of our conversation in the following format: <my request>; <your answer here>.\n");
        prompt.append("Do not print `Your Answer` string before your answer.\n");

        prompt.append("Here's the conversation log:\n");

        conversationLog.get().forEach(entry ->
                prompt.append("My request: ")
                        .append(entry.getKey())
                        .append("; ")
                        .append("Your previous answer: ")
                        .append(entry.getValue())
                        .append("\n"));

        prompt.append("\nHere's my new request: ").append(userInputMessage);
        prompt.append("\nYour answer: ");

        log.debug("Prompt: {}", prompt);

        return prompt.toString().trim();
    }
}
