package com.example.assassistant.domain;

import lombok.Builder;

@Builder
public
record OpenAIRequest(
        String model,
        String prompt,
        Double temperature,
        Integer max_tokens,
        Double top_p,
        Double frequency_penalty,
        Double presence_penalty
) {
}

record Usage(
        String prompt_tokens,
        String completion_tokens,
        String total_tokens
) {}

