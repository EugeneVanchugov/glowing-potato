package com.example.assassistant.service.telegram.skill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Skill {
    GENERATE_IMAGE(
            "Generate an image",
            List.of("prompt")
    );

    private final String description;
    private final List<String> parameters;

    public String parameter(String name) {
        return parameters.stream()
                .filter(parameter -> parameter.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parameter with name " + name));
    }
}
