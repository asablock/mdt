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
