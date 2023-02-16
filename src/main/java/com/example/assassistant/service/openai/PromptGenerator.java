package com.example.assassistant.service.openai;

import com.example.assassistant.domain.ConversationLog;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@AllArgsConstructor
public class PromptGenerator {
    private final ConversationLog conversationLog;

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
    public String buildPrompt(String userInputMessage) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Every your response has to be a valid JSON object.\n");
        prompt.append("It has to have the following format:\n");
        prompt.append("""
                {
                    "answer": "<your answer here>",
                    "context": {
                        "action": "<action name to perform as a Virtual Assistant>",
                        "parameters": [
                            {
                                "name": "<parameter name>",
                                "value": "<parameter value>"
                            }
                        ]
                    }
                }
                """
        );

        prompt.append("If there's no action to be parsed from my input, the context object should be null.\n");

        prompt.append("The action - is the one of the following:\n");
        prompt.append("1. `GenerateImage` - to generate an image.\n");

        prompt.append("Action `GenerateImage` has the following parameters:\n");
        prompt.append("1. `prompt` - the prompt for the image generation.\n");

        prompt.append("I want you to remember the context of our conversation.\n");

        prompt.append("Here's the conversation log:\n");
        conversationLog.get().forEach(entry ->
                prompt.append("My previous request: ")
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
