package io.github.asablock.mdt.toggle;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Toggle<T> extends ToggleNode {
    public final T defaultValue;
    private T value;
    private final Predicate<T> acceptable;
    private final BiConsumer<T, T> afterChanged;
    private final JsonCodec<T> jsonCodec;

    // command

    private final Function<T, String> toStringer;

    // called exactly once (while building /mtoggle command)
    private final CommandArgumentAppender commandArgumentAppender;

    // called while /mtoggle set executes
    private final ValueParser<T> valueParser;

    public Toggle(ToggleDirectory parent, String name, T defaultValue, Predicate<T> acceptable, BiConsumer<T, T> afterChanged, JsonCodec<T> jsonCodec, Function<T, String> toStringer, CommandArgumentAppender commandArgumentAppender, ValueParser<T> valueParser) {
        super(parent, name);
        Objects.requireNonNull(acceptable);
        Objects.requireNonNull(defaultValue);
        Objects.requireNonNull(commandArgumentAppender);
        Objects.requireNonNull(afterChanged);
        Objects.requireNonNull(valueParser);
        Objects.requireNonNull(toStringer);
        Objects.requireNonNull(jsonCodec);
        this.value = defaultValue;
        this.acceptable = acceptable;
        this.afterChanged = afterChanged;
        if (!acceptable.test(defaultValue)) {
            throw new IllegalArgumentException("defaultValue is not acceptable");
        }
        this.defaultValue = defaultValue;
        this.commandArgumentAppender = commandArgumentAppender;
        this.valueParser = valueParser;
        this.toStringer = toStringer;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public int reset() {
        return Boolean.TRUE.equals(set(defaultValue)) ? 1 : 0;
    }

    /**
     * @return {@code true} if given value is acceptable and triggered callback;
     * {@code false} if given value is acceptable but not triggered callback;
     * {@code null} if given value is not acceptable.
     */
    public Boolean set(T value) {
        if (canAccept(value)) {
            if (!this.value.equals(value)) {
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

    public void appendCommandArgument(ArgumentBuilder<FabricClientCommandSource, ?> parent, Executes executes) {
        commandArgumentAppender.append(parent, executes);
    }

    public T getInputValue(CommandContext<FabricClientCommandSource> context, int parentId) throws CommandSyntaxException {
        return valueParser.parse(context, parentId);
    }

    private static final BiConsumer<?, ?> DO_NOTHING = (a, b) -> {};

    @SuppressWarnings("unchecked")
    public static <T> BiConsumer<T, T> doNothing() {
        return (BiConsumer<T, T>) DO_NOTHING;
    }

    @FunctionalInterface
    public interface CommandArgumentAppender {
        void append(ArgumentBuilder<FabricClientCommandSource, ?> parent, Executes executes);
    }

    @FunctionalInterface
    public interface ValueParser<T> {
        T parse(CommandContext<FabricClientCommandSource> context, int parentId) throws CommandSyntaxException;
    }

    public interface JsonCodec<T> {
        JsonElement encode(T value);

        T decode(JsonElement e);

        static <E> JsonCodec<E> of(final Function<E, JsonElement> encoder, final Function<JsonElement, E> decoder) {
            return new JsonCodec<>() {
                @Override
                public JsonElement encode(E value) {
                    return encoder.apply(value);
                }

                @Override
                public E decode(JsonElement e) {
                    return decoder.apply(e);
                }
            };
        }
    }

    public JsonCodec<T> getJsonCodec() {
        return jsonCodec;
    }

    @Override
    public JsonElement encodeJson() {
        return jsonCodec.encode(value);
    }

    @Override
    public void decodeJson(JsonElement source) {
        set(jsonCodec.decode(source));
    }

    @Override
    protected void insert(StringBuilder sb) {
        sb.insert(0, getSimpleName());
    }

    @Override
    public Toggle<T> getAsToggle() {
        return this;
    }

    public interface Executes {
        int ID_DIRECT = -1;

        default Command<FabricClientCommandSource> direct() {
            return withParentId(ID_DIRECT);
        }

        Command<FabricClientCommandSource> withParentId(final int id);
    }
}
