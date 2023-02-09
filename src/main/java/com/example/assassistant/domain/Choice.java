package com.example.assassistant.domain;

public record Choice(
        String text,
        Integer index,
        String logprobs,
        String finish_reason
) {
}
