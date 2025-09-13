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
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class MdtCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mdt").executes(MdtCommand::executes));
    }

    private static final String GITHUB_URL = "https://github.com/asablock/mdt";
    private static final String ASABLOCK_URL = "https://github.com/asablock";

    public static int executes(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("command.mdt.mdt.about", FabricLoader.getInstance().getModContainer("mdt").orElseThrow().getMetadata().getVersion().getFriendlyString(), Text.literal("asablock").formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ASABLOCK_URL)))));
        context.getSource().sendFeedback(Text.translatable("command.mdt.mdt.github", Text.literal(GITHUB_URL).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB_URL)))));
        return Command.SINGLE_SUCCESS;
    }
}
