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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Collection;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ScoreboardCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mscoreboard")
                .then(literal("objectives")
                        .then(literal("list").executes(ScoreboardCommand::executeListObjectives)))
                .then(literal("players")
                        .then(literal("list").executes(ScoreboardCommand::executeListPlayers))));
    }

    public static int executeListObjectives(CommandContext<FabricClientCommandSource> context) {
        Collection<ScoreboardObjective> collection = context.getSource().getClient().getNetworkHandler().getScoreboard().getObjectives();
        if (collection.isEmpty()) {
            context.getSource().sendFeedback(Text.translatable("commands.scoreboard.objectives.list.empty"));
        } else {
            context.getSource().sendFeedback(Text.translatable("commands.scoreboard.objectives.list.success", collection.size(), Texts.join(collection, ScoreboardObjective::toHoverableText)));
        }

        return collection.size();
    }

    public static int executeListPlayers(CommandContext<FabricClientCommandSource> context) {
        Collection<ScoreHolder> collection = context.getSource().getClient().getNetworkHandler().getScoreboard().getKnownScoreHolders();
        if (collection.isEmpty()) {
            context.getSource().sendFeedback(Text.translatable("commands.scoreboard.players.list.empty"));
        } else {
            context.getSource().sendFeedback(Text.translatable("commands.scoreboard.players.list.success", collection.size(), Texts.join(collection, ScoreHolder::getStyledDisplayName)));
        }

        return collection.size();
    }
}
