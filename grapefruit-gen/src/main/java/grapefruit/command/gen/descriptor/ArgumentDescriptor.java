package grapefruit.command.gen.descriptor;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.VariableElement;

public class ArgumentDescriptor implements Descriptor {

    public static ArgumentDescriptor create(VariableElement element) {
        throw new RuntimeException();
    }

    @Override
    public void decorate(TypeSpec.Builder builder) {

    }

    public CodeBlock generateInitializer() {
        return CodeBlock.of("");
    }

    public String keyName() {
        return "";
    }
}
