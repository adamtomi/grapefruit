package grapefruit.command.compiler.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.lang.model.element.TypeElement;
import java.util.Collection;

public class NameCache {
    private final Multimap<TypeElement, Entry> knownNames = HashMultimap.create();

    public String generateUniqueName(TypeElement container, String originalName, String candidate) {
        if (!this.knownNames.containsKey(container)) {
            this.knownNames.put(container, new Entry(originalName, candidate));
            return candidate;
        }

        Collection<Entry> entries = this.knownNames.get(container);
        int matchingEntries = 0;
        for (Entry entry : entries) {
            if (entry.generatedName().equals(candidate)) {
                matchingEntries++;
                if (entry.originalName().equals(originalName)) return candidate;
            }
        }

        // Store new entry
        String newName = matchingEntries == 0
                ? candidate
                : "%s_%d".formatted(candidate, matchingEntries);
        this.knownNames.put(container, new Entry(originalName, newName));
        return newName;
    }

    record Entry(String originalName, String generatedName) {}
}
