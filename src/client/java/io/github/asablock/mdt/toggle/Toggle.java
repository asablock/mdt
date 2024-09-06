package io.github.asablock.mdt.toggle;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Toggle<T> {
    public final String name;
    public final T defaultValue;
    private T value;
    private final Predicate<T> acceptable;
    private final BiConsumer<T, T> afterChanged;

    // command

    private final Function<T, String> toStringer;

    // called exactly once (while building /mtoggle command)
    private final CommandNodeFactory commandNodeFactory;

    // called while /mtoggle set executes
    private final ValueParser<T> valueParser;

    public Toggle(String name, T defaultValue, Predicate<T> acceptable, BiConsumer<T, T> afterChanged, Function<T, String> toStringer, CommandNodeFactory commandNodeFactory, ValueParser<T> valueParser) {
        Objects.requireNonNull(acceptable);
        Objects.requireNonNull(name);
        Objects.requireNonNull(defaultValue);
        Objects.requireNonNull(commandNodeFactory);
        Objects.requireNonNull(afterChanged);
        Objects.requireNonNull(valueParser);
        Objects.requireNonNull(toStringer);
        this.name = name;
        this.value = defaultValue;
        this.acceptable = acceptable;
        this.afterChanged = afterChanged;
        if (!acceptable.test(defaultValue)) {
            throw new IllegalArgumentException("defaultValue is not acceptable");
        }
        this.defaultValue = defaultValue;
        this.commandNodeFactory = commandNodeFactory;
        this.valueParser = valueParser;
        this.toStringer = toStringer;
    }

    public boolean reset() {
        return set(defaultValue);
    }

    /**
     * @return {@code true} if given value is acceptable and triggered callback;
     * {@code false} if given value is acceptable but not triggered callback;
     * {@code null} if given value is not acceptable.
     */
    public Boolean set(T value) {
        if (canAccept(value)) {
            if (this.value.equals(value)) {
                T old = this.value;
                this.value = value;
                afterChanged.accept(old, value);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    public T get() {
        return value;
    }

    public boolean canAccept(T value) {
        return value != null && acceptable.test(value);
    }

    public String toString(T value) {
        return canAccept(value) ? toStringer.apply(value) : null;
    }

    public String valueToString() {
        return toStringer.apply(value);
    }

    public CommandNode<FabricClientCommandSource> createCommandNode(Command<FabricClientCommandSource> executes) {
        return commandNodeFactory.create(executes);
    }

    public T getInputValue(CommandContext<FabricClientCommandSource> context) {
        return valueParser.parse(context);
    }

    private static final BiConsumer<?, ?> DO_NOTHING = (a, b) -> {};

    @SuppressWarnings("unchecked")
    public static <T> BiConsumer<T, T> doNothing() {
        return (BiConsumer<T, T>) DO_NOTHING;
    }

    @FunctionalInterface
    public interface CommandNodeFactory {
        CommandNode<FabricClientCommandSource> create(Command<FabricClientCommandSource> executes);
    }

    @FunctionalInterface
    public interface ValueParser<T> {
        T parse(CommandContext<FabricClientCommandSource> context);
    }
}
