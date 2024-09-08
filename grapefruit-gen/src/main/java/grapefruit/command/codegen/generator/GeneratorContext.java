package grapefruit.command.codegen.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GeneratorContext {
    /* Name of the generator */
    private final String generator;
    /* We don't want duplicates, but care about the order */
    private final Set<FieldSpec> fields = new LinkedHashSet<>();
    private final Set<MethodSpec> methods = new LinkedHashSet<>();
    private final Set<StaticImport> staticImports = new LinkedHashSet<>();

    public GeneratorContext(String generator) {
        this.generator = requireNonNull(generator, "generator cannot be null");
    }

    public void include(FieldSpec field) {
        requireNonNull(field, "field cannot be null");
        this.fields.add(field);
    }

    public void include(MethodSpec method) {
        requireNonNull(method, "method cannot be null");
        this.methods.add(method);
    }

    public void importStatic(Class<?> from, String method) {
        this.staticImports.add(new StaticImport(from, method));
    }

    public Set<FieldSpec> fields() {
        return this.fields;
    }

    public Set<MethodSpec> methods() {
        return this.methods;
    }

    public Set<StaticImport> staticImports() {
        return this.staticImports;
    }

    public String generator() {
        return this.generator;
    }

    public record StaticImport(Class<?> from, String method) {
        public StaticImport {
            requireNonNull(from, "from cannot be null");
            requireNonNull(method, "method cannot be null");
        }
    }
}
