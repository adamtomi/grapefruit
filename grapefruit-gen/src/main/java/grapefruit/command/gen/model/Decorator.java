package grapefruit.command.gen.model;

import com.squareup.javapoet.TypeSpec;

@FunctionalInterface
public interface Decorator {

    void decorate(TypeSpec.Builder builder);
}
