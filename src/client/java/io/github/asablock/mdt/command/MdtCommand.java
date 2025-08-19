package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class MdtCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mdt").executes(MdtCommand::executes));
    }

    private static final URI GITHUB_URI = URI.create("https://github.com/asablock/mdt");

    public static int executes(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("command.mdt.mdt.about", FabricLoader.getInstance().getModContainer("mdt").orElseThrow().getMetadata().getVersion().getFriendlyString(), "asablock"));
        context.getSource().sendFeedback(Text.translatable("command.mdt.mdt.github", Text.of(GITHUB_URI)));
        return Command.SINGLE_SUCCESS;
    }
}
