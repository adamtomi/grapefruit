package grapefruit.command.gen.util;

import com.squareup.javapoet.TypeSpec;

@FunctionalInterface
public interface Decorator {

    void decorate(TypeSpec.Builder builder);
}
