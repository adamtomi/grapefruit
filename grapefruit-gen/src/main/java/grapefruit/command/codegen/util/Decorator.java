package grapefruit.command.codegen.util;

import com.squareup.javapoet.TypeSpec;

@FunctionalInterface
public interface Decorator {

    void decorate(TypeSpec.Builder builder);
}
