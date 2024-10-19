package grapefruit.command.runtime.dispatcher.input;

import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.dispatcher.syntax.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;

/**
 * Provides utility functions for {@link ArgumentMapper mappers}
 * for reading user input.
 */
public interface StringReader {
    /* Both of these fields are used by StringReader#readQuotable */
    char SINGLE_QUOTE = '\'';
    char DOUBLE_QUOTE = '"';

    /**
     * Checks and returns whether there still is unconsumed
     * input that can be read.
     *
     * @return Whether there still is input to read
     */
    boolean hasNext();

    /**
     * Moves the cursor by one and reads the char located at that
     * index.
     *
     * @return The read character
     * @throws CommandSyntaxException If there is nothing to read
     */
    char next() throws CommandSyntaxException;


    /**
     * Returns a single string surrounded by whitespace without
     * moving the cursor, or null if no string could be read.
     *
     * @return The string or null
     */
    @Nullable String peekSingle();

    /**
     * Reads a single string surrounded by whitespace.
     *
     * @return The string
     * @throws CommandSyntaxException If there is nothing to read
     */
    String readSingle() throws CommandSyntaxException;

    /**
     * Reads either a string between quotation marks or
     * a single string.
     *
     * @return The string
     * @throws CommandSyntaxException If there is nothing to read
     */
    String readQuotable() throws CommandSyntaxException;

    /**
     * Reads the remaining input.
     *
     * @return The remaining input
     * @throws CommandSyntaxException If there is nothing to read
     */
    String readRemaining() throws CommandSyntaxException;

    /**
     * Returns the consumed part of the original input. Consumed
     * parts are parts that have been read successfully by any
     * of the reader's read methods.
     *
     * @return The consumed part of the input
     */
    String consumed();

    /**
     * Returns the original input wrapped this reader instance.
     *
     * @return The original input
     */
    String unwrap();
}
