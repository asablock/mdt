package io.github.asablock.mdt.toggle;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static io.github.asablock.mdt.toggle.Toggle.JsonCodec.of;

public class ToggleFactory {
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

    public static ToggleDirectory ofDir(ToggleDirectory parent, String name) {
        return new ToggleDirectory(parent, name);
    }
}
