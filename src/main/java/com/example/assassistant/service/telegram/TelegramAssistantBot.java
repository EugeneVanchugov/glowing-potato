package com.example.assassistant.service.telegram;

import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.domain.GPTFormattedResponse;
import com.example.assassistant.domain.OpenAIResponse;
import com.example.assassistant.service.asr.SpeechService;
import com.example.assassistant.service.openai.OpenAIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class TelegramAssistantBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramAssistantBot.class);

    private final TelegramBot bot;
    private final OpenAIClient openAIClient;
    private final ConversationLog conversationLog;
    private final SpeechService speechService;

    /**
     * Telegram bot listener that processes incoming messages.
     */
    public void startListening() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() == null) {
                    log.error("Received update without message: {}", update);
                    return;
                }
                // Voice Message
                if (update.message().voice() != null) {
                    processVoiceMessage(update);
                }

                // Text Message
                if (update.message().text() != null) {
                    processTextInput(update);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Extracts voice from incoming Telegram message and sends it to Google Cloud Speech API for Speech-to-Text conversion.
     * Then sends the recognized text to OpenAI API for GPT-3 processing.
     *
     * @param update Telegram update with voice message
     */
    public void processVoiceMessage(Update update) {
        Long chatId = update.message().chat().id();
        String audioFileURL = extractAudioFileURL(update);

        String recognizedText = speechService.recognizeAudio(audioFileURL);

        sendPromptToOpenAI(chatId, recognizedText);
    }

    /**
     * Sends the text from incoming Telegram message to OpenAI API for GPT-3 processing.
     *
     * @param update Telegram update with text message
     */
    public void processTextInput(Update update) {
        Long chatId = update.message().chat().id();
        String userInputMessage = update.message().text();

        sendPromptToOpenAI(chatId, userInputMessage);
    }

    /**
     * Sends the prompt to OpenAI API for GPT-3 processing.
     * Subscribes to the Mono<OpenAIResponse> and sends the answer to the Telegram chat.
     *
     * @param chatId    Telegram chat ID
     * @param userInput User input as string
     */
    private void sendPromptToOpenAI(Long chatId, String userInput) {
        openAIClient.sendPrompt(userInput)
                .subscribe(
                        openAIResponse -> {
                            log.info("GPT-3 Answer: {}", openAIResponse);

                            bot.execute(new SendMessage(chatId, openAIResponse.answer()));
                            conversationLog.add(userInput, openAIResponse.answer());
                        }
                );
    }

    private String extractAudioFileURL(Update update) {
        String fileId = update.message().voice().fileId();

        File voiceMessageFile = bot.execute(new GetFile(fileId)).file();
        return bot.getFullFilePath(voiceMessageFile);
    }
}
