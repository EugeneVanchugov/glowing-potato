package com.example.assassistant.service.telegram.processor;

import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.domain.GPTFormattedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class SimpleTextProcessor implements GPT3ResponseProcessor<GPTFormattedResponse> {
    private final ConversationLog conversationLog;
    private final TelegramBot bot;

    @Override
    @NotNull
    public Consumer<GPTFormattedResponse> process(Long chatId, String userInput) {
        return openAIResponse -> {
            GPTFormattedResponse.Context context = openAIResponse.context();


            bot.execute(new SendMessage(chatId, openAIResponse.answer()));
            conversationLog.add(userInput, openAIResponse.answer());
        };
    }
}
