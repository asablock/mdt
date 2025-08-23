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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.asablock.mdt.mixin.DefaultPosArgumentAccessor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ClientBlockPosArgumentType implements ArgumentType<DefaultPosArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");

    public static ClientBlockPosArgumentType blockPos() {
        return new ClientBlockPosArgumentType();
    }

    public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        DefaultPosArgument argument = context.getArgument(name, DefaultPosArgument.class);
        DefaultPosArgumentAccessor acc = (DefaultPosArgumentAccessor) argument;
        Vec3d vec3d = context.getSource().getPlayer().getPos();
        return BlockPos.ofFloored(new Vec3d(acc.getX().toAbsoluteCoordinate(vec3d.x), acc.getY().toAbsoluteCoordinate(vec3d.y), acc.getZ().toAbsoluteCoordinate(vec3d.z)));
    }

    public DefaultPosArgument parse(StringReader stringReader) throws CommandSyntaxException {
        return DefaultPosArgument.parse(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource)) {
            return Suggestions.empty();
        } else {
            String string = builder.getRemaining();
            Collection<CommandSource.RelativePosition> collection;
            if (!string.isEmpty() && string.charAt(0) == '^') {
                collection = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
            } else {
                collection = ((CommandSource)context.getSource()).getBlockPositionSuggestions();
            }

            return CommandSource.suggestPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

