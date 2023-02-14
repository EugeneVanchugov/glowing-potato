package com.example.assassistant.config;

import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.service.asr.GoogleCloudSpeechClient;
import com.example.assassistant.service.asr.SpeechService;
import com.example.assassistant.service.openai.OpenAIClient;
import com.example.assassistant.service.telegram.TelegramAssistantBot;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@org.springframework.context.annotation.Configuration
public class Configuration {
    public static final String OPENAI_API_KEY = "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";
    public static final String BOT_TOKEN = "6230426113:AAFD5ELkvVnVQGMlrOFU96NE3GpElItgvFo";
    public static final String OPENAI_API = "https://api.openai.com/v1";
    public static final int AUDIO_CHUNK_SIZE = 1024;
    public static final int SAMPLE_RATE_HERTZ = 16000;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(OPENAI_API)
                .defaultHeader("Authorization", "Bearer " + Configuration.OPENAI_API_KEY)
                .build();
    }

    @Bean
    public ConversationLog conversationLog() {
        return new ConversationLog();
    }

    @Bean
    public OpenAIClient openAIClient(
            WebClient webClient,
            ConversationLog conversationLog
    ) {
        return new OpenAIClient(
                webClient,
                conversationLog
        );
    }

    @Bean
    public SpeechService googleCloudSpeechService(GoogleCloudSpeechClient googleCloudSpeechClient) {
        return new SpeechService(googleCloudSpeechClient);
    }

    @Bean
    public GoogleCloudSpeechClient googleCloudSpeechClient() {
        return new GoogleCloudSpeechClient();
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(BOT_TOKEN);
    }

    @Bean
    public TelegramAssistantBot telegramAssistantBot(
            TelegramBot telegramBot,
            OpenAIClient openAIClient,
            ConversationLog conversationLog,
            SpeechService speechService
    ) {
        return new TelegramAssistantBot(
                telegramBot,
                openAIClient,
                conversationLog,
                speechService
        );
    }
}
