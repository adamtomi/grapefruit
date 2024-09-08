package grapefruit.command.codegen.util;

import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import grapefruit.command.util.key.Key;

public final class CodeBlockUtil {
    private CodeBlockUtil() {}

    public static CodeBlock typeToken(TypeName typeName) {
        String body = typeName instanceof ParameterizedTypeName
                ? "new $T<$T>() {}"
                : "$T.of($T.class)";
        return CodeBlock.of(body, TypeToken.class, typeName);
    }

    public static CodeBlock key(TypeName typeName, String name) {
        CodeBlock typeToken = typeToken(typeName);
        if (name == null) {
            return CodeBlock.of("$T.of($L)", Key.class, typeToken);
        } else {
            return CodeBlock.of("$T.named($L, $S)", Key.class, typeToken, name);
        }
    }
}
