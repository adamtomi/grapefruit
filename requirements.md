## Basic requirements
- Command handler methods should be annotated with `@CommandDefinition`
- Code should be generated for every command handler method in order to avoid invoking methods via reflection
- Command parameters can be of any type, the conversion of strings into these types are to be handled by mappers.
- Bindings shall be used to bind mappers, types and specifier annotations. The following example was inspired by **`Guice`**:
```java
void setup(CommandContext context) {
    bind(String.class).to(StringMapper.INSTANCE);
    bind(String.class).annotatedWith(Quotable.class).to(StringMapper.INSTANCE);
    bind(CommandSource.class).toSupplier(() -> context.getSource());
}
```
- The mapped arguments as well as other related command information (such as initial command line, command source) are to be stored in a `CommandContext` class.
- Upon code generation a new class (implementing `CommandCallable`) should be generated. This class should have a `call` method which invokes the original command method. It should also store relevant command information, such as route, permission, and a list of parameters (name, type, modifiers if any)
- Command parameters may be of two kinds: 1) required, having a fixed position in the parameters list, or 2) optional. Optional parameters will be treated as flags (a `@Flag` annotation should indicate this)
- Flags should have a long name, such as `--some-flag`, but they also may specify a shorthand, `-s` for instance. The command parser should be able to handle grouped flags. Suppose there are three flags in a parameter list, `-a`, `-b` and `-c`; these grouped together would be `-abc`.
- As flags are easily identified because of their names, their position in the command line is not fixed, however they must be specified after literal arguments (command names).
- Flags of boolean type are considered "presence flags". The boolean value doesn't need to be set as the presence of the flag name itself will determine the value

## Condition requirements
- Grapefruit should support command conditions. A command condition will decide based on the current context if the execution of the pending command should proceed.
- A single command handler method may have multiple conditions; in which case all of them need to pass.
- The condition API should provide a simple way (most likely through annotations) to define and use conditions.

## To figure out
- The entirety of command conditions
- whether command permissions should be done using conditions or they should have a separate permission system
- How to provide the command source to the handler method. Either we'll need a `@Arg` annotation to mark non-flag command arguments as arguments, or we'll have to have a special `@Source` annotation to indicate that the command source object should be passed to a specific parameter. The first option is more flexible, but annotating everything with `@Arg` is not convenient.
- Internal message system. Do we need it? If so, what's the most ideal approach? Can we get away with throwing various kinds of exceptions instead? Questions, questions...
