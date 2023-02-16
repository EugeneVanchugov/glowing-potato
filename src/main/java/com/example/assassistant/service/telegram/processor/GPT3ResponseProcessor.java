package com.example.assassistant.service.telegram.processor;

import java.util.function.Consumer;

public interface GPT3ResponseProcessor<R> {
    Consumer<R> process(Long chatId, String userInput);
}
