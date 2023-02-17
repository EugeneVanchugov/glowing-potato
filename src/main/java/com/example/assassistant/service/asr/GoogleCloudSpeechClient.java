package com.example.assassistant.service.asr;

import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.assassistant.config.Configuration.SAMPLE_RATE_HERTZ;

@Slf4j
public class GoogleCloudSpeechClient {
    private final List<String> languageCodes = List.of("en-US", "ru-RU");
    private final RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(AudioEncoding.WEBM_OPUS)
            .addAllAlternativeLanguageCodes(languageCodes)
            .setSampleRateHertz(SAMPLE_RATE_HERTZ)
            .build();

    public String recognizeAudio(byte[] audioData) {
        Objects.requireNonNull(audioData, "Audio data must not be null");

        ByteString audioByteString = ByteString.copyFrom(audioData);

        try (SpeechClient speechClient = SpeechClient.create()) {
            // Builds the sync recognize request
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioByteString)
                    .build();

            // Performs speech recognition on the audio data
            RecognizeResponse response = speechClient.recognize(config, audio);

            return Optional.of(response.getResultsList())
                    .stream()
                    .filter(it -> !it.isEmpty())
                    .map(this::getTranscript)
                    .peek(it -> log.info("Recognized text: {}", it))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No speech recognized"));
        } catch (IOException e) {
            log.error("Error while recognizing audio");
            throw new RuntimeException(e);
        }
    }

    private String getTranscript(List<SpeechRecognitionResult> speechRecognitionResults) {
        return speechRecognitionResults
                .get(0)
                .getAlternativesList()
                .get(0)
                .getTranscript();
    }
}
