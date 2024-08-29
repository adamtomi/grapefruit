package grapefruit.command.gen.util;

import com.squareup.javapoet.CodeBlock;

import java.util.Iterator;

public final class CodeBlockUtil {
    private CodeBlockUtil() {}

    public static CodeBlock join(String delimiter, Iterable<CodeBlock> blocks) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for (Iterator<CodeBlock> iter = blocks.iterator(); iter.hasNext(); ) {
            builder.add(iter.next());
            if (iter.hasNext()) builder.add(delimiter);
        }

        return builder.build();
    }
}
