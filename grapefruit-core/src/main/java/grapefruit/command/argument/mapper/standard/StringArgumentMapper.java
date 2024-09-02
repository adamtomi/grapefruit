package grapefruit.command.argument.mapper.standard;

import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.mapper.WrappedArgumentMapper;

public interface StringArgumentMapper extends ArgumentMapper<String> {
    ArgumentMapper<String> SINGLE = WrappedArgumentMapper.of((context, reader) -> reader.readSingle());
    ArgumentMapper<String> QUOTABLE = WrappedArgumentMapper.of((context, reader) -> reader.readQuotable());
    ArgumentMapper<String> GREEDY = WrappedArgumentMapper.of((context, reader) -> reader.readRemaining());

    String QUOTABLE_NAME = "__QUOTABLE__";
    String GREEDY_NAME = "__GREEDY__";
}
