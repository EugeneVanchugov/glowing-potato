package com.example.assassistant.service.asr;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

@AllArgsConstructor
public class SpeechService {
    private static final Logger log = LoggerFactory.getLogger(SpeechService.class);

    private final GoogleCloudSpeechClient speechClient;
    public String recognizeAudio(String audioFileURL) {
        Objects.requireNonNull(audioFileURL, "Audio File URL must not be null");

        log.info("Recognize voice message from URL: " + audioFileURL);

        return speechClient.recognizeAudio(readAudioFromURL(audioFileURL));
    }

    private static byte[] readAudioFromURL(String url) {
        try {
            URL audioUrl = new URL(url);

            try (
                    InputStream inputStream = audioUrl.openStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
