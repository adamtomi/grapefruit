package grapefruit.command.dispatcher.input;

import grapefruit.command.dispatcher.CommandSyntaxException;

/**
 * Provides utility functions for {@link grapefruit.command.argument.mapper.ArgumentMapper mappers}
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
     * Constructs a new {@link StringReaderImpl} instance wrapping
     * the provided input.
     *
     * @param input The input to read from
     * @return The constructed reader
     */
    static StringReader wrap(String input) {
        return new StringReaderImpl(input);
    }
}
