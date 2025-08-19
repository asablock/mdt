package io.github.asablock.mdt.command.argument;

import com.mojang.serialization.Codec;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Supplier;

public class PEnumArgumentType<T extends Enum<T> & StringIdentifiable> extends EnumArgumentType<T> {
    public PEnumArgumentType(Codec<T> codec, Supplier<T[]> valuesSupplier) {
        super(codec, valuesSupplier);
    }

    public PEnumArgumentType(Class<T> clazz, Codec<T> codec) {
        super(codec, clazz::getEnumConstants);
    }

    public PEnumArgumentType(Class<T> clazz) {
        super(StringIdentifiable.createCodec(clazz::getEnumConstants), clazz::getEnumConstants);
    }
}
