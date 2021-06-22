package grapefruit.command.parameter;

import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

public record CommandParameter(int index,
                               @NotNull TypeToken<?> type,
                               @NotNull AnnotationList modifiers,
                               boolean isOptional) {}
