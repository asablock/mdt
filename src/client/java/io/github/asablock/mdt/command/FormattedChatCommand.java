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

package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.command.argument.PEnumArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class FormattedChatCommand {
    public static final StringBuilder STRING_BUILDER = new StringBuilder();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mformattedchat")
                .then(literal("append").then(argument("chat", StringArgumentType.greedyString()).executes(FormattedChatCommand::executeAppend)))
                .then(literal("preview").executes(FormattedChatCommand::executePreview))
                .then(literal("addformat").then(argument("format", new PEnumArgumentType<>(Formatting.class)).executes(FormattedChatCommand::executeAddFormat)))
                .then(literal("send").executes(FormattedChatCommand::executeSend))
                .then(literal("clear").executes(FormattedChatCommand::executeClear))
        );
    }

    public static int executeAppend(CommandContext<FabricClientCommandSource> context) {
        String chat = StringArgumentType.getString(context, "chat");
        STRING_BUILDER.append(chat);
        return Command.SINGLE_SUCCESS;
    }

    public static int executePreview(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal(STRING_BUILDER.toString()));
        return Command.SINGLE_SUCCESS;
    }

    public static int executeAddFormat(CommandContext<FabricClientCommandSource> context) {
        Formatting formatting = context.getArgument("format", Formatting.class);
        STRING_BUILDER.append(formatting);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSend(CommandContext<FabricClientCommandSource> context) {
        context.getSource().getClient().getNetworkHandler().sendChatMessage(STRING_BUILDER.toString());
        STRING_BUILDER.setLength(0);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeClear(CommandContext<FabricClientCommandSource> context) {
        STRING_BUILDER.setLength(0);
        return Command.SINGLE_SUCCESS;
    }
}
