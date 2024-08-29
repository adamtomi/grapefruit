package grapefruit.command.gen.descriptor;

import com.squareup.javapoet.TypeSpec;

@FunctionalInterface
public interface Descriptor {

    void decorate(TypeSpec.Builder builder);
}
