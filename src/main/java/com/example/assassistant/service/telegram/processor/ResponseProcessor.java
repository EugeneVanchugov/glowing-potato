package com.example.assassistant.service.telegram.processor;

import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.domain.GPTFormattedResponse;
import com.example.assassistant.service.telegram.skill.Skill;
import com.example.assassistant.service.telegram.skill.SkillExecutor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ResponseProcessor {
    private final TelegramBot bot;
    private final ConversationLog conversationLog;
    private final SkillExecutor skillExecutor;

    @NotNull
    public Consumer<GPTFormattedResponse> process(Long chatId, String userInput) {
        return openAIResponse -> {
            GPTFormattedResponse.Context context = openAIResponse.context();
            Skill skill = Skill.valueOf(context.action());

            skillExecutor.execute(chatId, skill, context);

            bot.execute(new SendMessage(chatId, openAIResponse.answer()));
            conversationLog.add(userInput, openAIResponse.answer());
        };
    }
}
