package io.github.asablock.mdt.toggle;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static io.github.asablock.mdt.toggle.Toggle.JsonCodec.of;

public class ToggleFactory {
    public static ToggleDirectory ofDir(ToggleDirectory parent, String name) {
        return new ToggleDirectory(parent, name);
    }

    public static Toggle<Boolean> ofBool(ToggleDirectory parent, String name, boolean defaultValue) {
        return new Toggle<>(parent, name, defaultValue, b -> b instanceof Boolean, Toggle.doNothing(),
                of(JsonPrimitive::new, JsonElement::getAsBoolean), Object::toString,
                (p, executes) -> p.then(argument("value", BoolArgumentType.bool()).executes(executes.direct())), (ctx, parentId) -> BoolArgumentType.getBool(ctx, "value"));
    }

    public static Toggle<Boolean> ofBool(ToggleDirectory parent, String name, boolean defaultValue, BiConsumer<Boolean, Boolean> afterChanged) {
        return new Toggle<>(parent, name, defaultValue, b -> b instanceof Boolean, afterChanged,
                of(JsonPrimitive::new, JsonElement::getAsBoolean), Object::toString,
                (p, executes) -> p.then(argument("value", BoolArgumentType.bool()).executes(executes.direct())), (ctx, parentId) -> BoolArgumentType.getBool(ctx, "value"));
    }

    public static <E extends Enum<E> & StringIdentifiable> Toggle<E> ofEnum(ToggleDirectory parent, String name, E defaultValue, final Class<E> clazz) {
        final E[] es = clazz.getEnumConstants();
        final Map<String, E> keyMap = Arrays.stream(es).collect(Collectors.toMap(StringIdentifiable::asString, Function.identity()));
        return new Toggle<>(parent, name, defaultValue, clazz::isInstance, Toggle.doNothing(),
                of(value -> new JsonPrimitive(value.asString()), e -> keyMap.get(e.getAsString())),
                StringIdentifiable::asString, (p, executes) -> {
                    for (int i = 0; i < es.length; i++) {
                        p.then(literal(es[i].asString()).executes(executes.withParentId(i)));
                    }
                }, (context, parentId) -> es[parentId]);
    }

    public static <E extends Enum<E> & StringIdentifiable> Toggle<E> ofEnum(ToggleDirectory parent, String name, E defaultValue, final Class<E> clazz, BiConsumer<E, E> afterChanged) {
        final E[] es = clazz.getEnumConstants();
        final Map<String, E> keyMap = Arrays.stream(es).collect(Collectors.toMap(StringIdentifiable::asString, Function.identity()));
        return new Toggle<>(parent, name, defaultValue, clazz::isInstance, afterChanged,
                of(value -> new JsonPrimitive(value.asString()), e -> keyMap.get(e.getAsString())),
                StringIdentifiable::asString, (p, executes) -> {
                    for (int i = 0; i < es.length; i++) {
                        p.then(literal(es[i].asString()).executes(executes.withParentId(i)));
                    }
                }, (context, parentId) -> es[parentId]);
    }

    private static final SimpleCommandExceptionType NOT_QUOTED_STRING_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.mdt.toggle.not_quoted_string"));

    public static Toggle<String> ofQuotedString(ToggleDirectory parent, String name, String defaultValue) {
        return new Toggle<>(parent, name, defaultValue, s -> s instanceof String, Toggle.doNothing(),
                of(JsonPrimitive::new, JsonElement::getAsString), Function.identity(),
                (p, executes) -> p.then(argument("value", StringArgumentType.greedyString()).executes(executes.direct())),
                (context, parentId) -> {
                    String str = StringArgumentType.getString(context, "value");
                    if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
                        return str.substring(1, str.length() - 1);
                    } else {
                        throw NOT_QUOTED_STRING_EXCEPTION.create();
                    }
                });
    }

    public static Toggle<String> ofQuotedString(ToggleDirectory parent, String name, String defaultValue, BiConsumer<String, String> afterChanged) {
        return new Toggle<>(parent, name, defaultValue, s -> s instanceof String, afterChanged,
                of(JsonPrimitive::new, JsonElement::getAsString), Function.identity(),
                (p, executes) -> p.then(argument("value", StringArgumentType.greedyString()).executes(executes.direct())),
                (context, parentId) -> {
                    String str = StringArgumentType.getString(context, "value");
                    if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
                        return str.substring(1, str.length() - 1);
                    } else {
                        throw NOT_QUOTED_STRING_EXCEPTION.create();
                    }
                });
    }

    /**
     * @param min inclusive
     * @param max inclusive
     */
    public static Toggle<Integer> ofInt(ToggleDirectory parent, String name, int defaultValue, int min, int max) {
        return new Toggle<>(parent, name, defaultValue, i -> i >= min && i <= max, Toggle.doNothing(),
                of(JsonPrimitive::new, JsonElement::getAsInt), Object::toString,
                (p, executes) -> p.then(argument("value", IntegerArgumentType.integer(min, max)).executes(executes.direct())),
                ((context, parentId) -> IntegerArgumentType.getInteger(context, "value")));
    }

    /**
     * @param min inclusive
     * @param max inclusive
     */
    public static Toggle<Integer> ofInt(ToggleDirectory parent, String name, int defaultValue, int min, int max, BiConsumer<Integer, Integer> afterChanged) {
        return new Toggle<>(parent, name, defaultValue, i -> i >= min && i <= max, afterChanged,
                of(JsonPrimitive::new, JsonElement::getAsInt), Object::toString,
                (p, executes) -> p.then(argument("value", IntegerArgumentType.integer(min, max)).executes(executes.direct())),
                ((context, parentId) -> IntegerArgumentType.getInteger(context, "value")));
    }

    public static Toggle<Integer> ofInt(ToggleDirectory parent, String name, int defaultValue) {
        return ofInt(parent, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static Toggle<Integer> ofInt(ToggleDirectory parent, String name, int defaultValue, BiConsumer<Integer, Integer> afterChanged) {
        return ofInt(parent, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, afterChanged);
    }
}
