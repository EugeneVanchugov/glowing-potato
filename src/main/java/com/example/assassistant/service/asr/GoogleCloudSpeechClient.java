package com.example.assassistant.service.asr;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class GoogleCloudSpeechClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudSpeechClient.class);

    public String recognizeAudio(byte[] audioData) {
        Objects.requireNonNull(audioData, "Audio data must not be null");

        ByteString audioByteString = ByteString.copyFrom(audioData);

        try (SpeechClient speechClient = SpeechClient.create()) {
            // Builds the sync recognize request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ru-RU")
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioByteString)
                    .build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            if (results.isEmpty()) {
                log.info("No speech recognized");
                return null;
            }

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                String transcript = alternative.getTranscript();

                log.info("Recognized text: {}", transcript);

                return transcript;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
