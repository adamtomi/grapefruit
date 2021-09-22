package grapefruit.command.parameter;

import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

@Deprecated
public record CommandParameter0(int index,
                                @NotNull TypeToken<?> type,
                                @NotNull AnnotationList modifiers,
                                boolean isOptional) {}
