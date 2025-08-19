package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SendChatCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("msendchat").then(argument("content", StringArgumentType.greedyString()).executes(SendChatCommand::execute)));
    }

    public static int execute(CommandContext<FabricClientCommandSource> context) {
        String content = StringArgumentType.getString(context, "content");
        context.getSource().getClient().getNetworkHandler().sendChatMessage(content);
        return Command.SINGLE_SUCCESS;
    }
}
