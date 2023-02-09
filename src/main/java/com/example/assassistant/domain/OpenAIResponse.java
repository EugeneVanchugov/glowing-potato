package com.example.assassistant.domain;

import java.util.List;

public
record OpenAIResponse(
        String id,
        String object,
        Long created,
        String model,
        List<Choice> choices,
        Usage usage
) {
}
