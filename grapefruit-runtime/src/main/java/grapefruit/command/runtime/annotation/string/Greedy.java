package grapefruit.command.runtime.annotation.string;

import grapefruit.command.runtime.annotation.meta.MappedBy;
import grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the argument is to be mapped by {@link StringArgumentMapper#greedy()}
 */
@MappedBy(StringArgumentMapper.GREEDY_NAME)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Greedy {
}
