package grapefruit.command.codegen.util;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import java.util.List;

public final class TypeNameUtil {
    private TypeNameUtil() {}

    public static List<TypeName> flattenTypeNames(TypeName type) {
        return type instanceof ParameterizedTypeName parameterized
                ? parameterized.typeArguments
                : List.of(type);
    }

    public static TypeName toTypeName(Element element) {
        return TypeName.get(element.asType());
    }

    public static TypeName unboxSafe(TypeName type) {
        return type.isBoxedPrimitive() ? type.unbox() : type;
    }
}
