package com.example.assassistant.service.telegram.skill;

import com.example.assassistant.domain.GPTFormattedResponse;
import com.example.assassistant.service.openai.OpenAIClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
public class SkillExecutor {

    private final TelegramBot bot;
    private final OpenAIClient openAIClient;

    public void execute(
            Long chatId,
            Skill skill,
            GPTFormattedResponse.Context context
    ) {
        Consumer<String> onImageUrl =
                imageUrl -> bot.execute(new SendPhoto(chatId, imageUrl));

        Consumer<String> generateImageSkillAction =
                prompt -> openAIClient
                        .generateImage(prompt)
                        .subscribe(onImageUrl);

        switch (skill) {
            case GENERATE_IMAGE -> new GenerateImageSkill().execute(context, generateImageSkillAction);
            default -> log.error("No skill found for {}", skill);
        }
    }
}
