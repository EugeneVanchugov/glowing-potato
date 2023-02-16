package com.example.assassistant.config;

import com.example.assassistant.domain.ConversationLog;
import com.example.assassistant.domain.GPTFormattedResponse;
import com.example.assassistant.service.asr.GoogleCloudSpeechClient;
import com.example.assassistant.service.asr.SpeechService;
import com.example.assassistant.service.openai.OpenAIClient;
import com.example.assassistant.service.openai.PromptGenerator;
import com.example.assassistant.service.openai.ResponseParser;
import com.example.assassistant.service.telegram.TelegramAssistantBot;
import com.example.assassistant.service.telegram.processor.GPT3ResponseProcessor;
import com.example.assassistant.service.telegram.processor.SimpleTextProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public PromptGenerator promptGenerator(ConversationLog conversationLog) {
        return new PromptGenerator(conversationLog);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ResponseParser responseParser(
            ObjectMapper objectMapper
    ) {
        return new ResponseParser(objectMapper);
    }

    @Bean
    public OpenAIClient openAIClient(
            WebClient webClient,
            PromptGenerator promptGenerator,
            ResponseParser responseParser
    ) {
        return new OpenAIClient(
                webClient,
                promptGenerator,
                responseParser
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
    public GPT3ResponseProcessor<GPTFormattedResponse> simpleTextProcessor(
            ConversationLog conversationLog,
            TelegramBot bot
    ) {
        return new SimpleTextProcessor(conversationLog, bot);
    }

    @Bean
    public TelegramAssistantBot telegramAssistantBot(
            TelegramBot telegramBot,
            OpenAIClient openAIClient,
            SpeechService speechService,
            GPT3ResponseProcessor<GPTFormattedResponse> simpleTextProcessor
    ) {
        return new TelegramAssistantBot(
                telegramBot,
                openAIClient,
                speechService,
                simpleTextProcessor
        );
    }
}
