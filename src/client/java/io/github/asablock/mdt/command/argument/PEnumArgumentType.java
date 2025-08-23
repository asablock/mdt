/*
 * This file is part of mdt. mdt is a client-side mod for Minecraft.
 * Copyright (C) 2025  asablock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
