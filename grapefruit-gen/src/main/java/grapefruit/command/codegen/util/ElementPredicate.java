package grapefruit.command.codegen.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static grapefruit.command.codegen.util.TypeNameUtil.toTypeName;
import static java.util.Objects.requireNonNull;

public class ElementPredicate implements Predicate<Element> {
    private final Set<Modifier> illegalModifiers;
    private final ElementKind expectedKind;

    private ElementPredicate(Set<Modifier> illegalModifiers, ElementKind expectedKind) {
        this.illegalModifiers = requireNonNull(illegalModifiers, "illegalModifiers cannot be null");
        this.expectedKind = requireNonNull(expectedKind, "expectedKind cannot be null");
    }

    @Override
    public boolean test(Element element) {
        return Collections.disjoint(this.illegalModifiers, element.getModifiers())
                && element.getKind().equals(this.expectedKind);
    }

    public void ensure(Element element) {
        if (!test(element)) {
            throw new IllegalStateException("Element '%s' is expected to be a(n) '%s', and cannot have the following modifiers: %s".formatted(
                    toTypeName(element),
                    this.expectedKind.name(),
                    this.illegalModifiers.stream().map(Modifier::name).collect(Collectors.joining(", "))
            ));
        }
    }

    public static Builder expect(ElementKind expectedKind) {
        return new Builder(expectedKind);
    }

    public static final class Builder {
        private final ElementKind expectedKind;
        private final Set<Modifier> illegalModifiers = new HashSet<>();

        private Builder(ElementKind expectedKind) {
            this.expectedKind = requireNonNull(expectedKind, "expectedKind cannot be null");
        }

        public Builder forbid(Modifier... modifiers) {
            for (Modifier modifier : modifiers) this.illegalModifiers.add(requireNonNull(modifier, "modifier cannot be null"));
            return this;
        }

        public ElementPredicate build() {
            return new ElementPredicate(this.illegalModifiers, this.expectedKind);
        }
    }
}
