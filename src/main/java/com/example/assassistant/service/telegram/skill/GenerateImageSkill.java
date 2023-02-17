package com.example.assassistant.service.telegram.skill;

import com.example.assassistant.domain.GPTFormattedResponse.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class GenerateImageSkill {
    private final Skill skill = Skill.GENERATE_IMAGE;

    public void execute(
            Context context,
            Consumer<String> onPromptValue
    ) {
        log.info("Generating image");

        context.parameters()
                .stream()
                .filter(parameter -> Objects.equals(parameter.name(), skill.parameter("prompt")))
                .map(Context.Parameter::value)
                .findFirst()
                .ifPresentOrElse(
                        onPromptValue,
                        () -> log.error("No prompt parameter found in context")
                );
    }
}
