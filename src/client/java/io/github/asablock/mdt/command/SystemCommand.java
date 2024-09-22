package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SystemCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("msystem")
                .then(literal("gc").executes(SystemCommand::executeGc))
                .then(literal("exit").then(argument("status", IntegerArgumentType.integer()).executes(SystemCommand::executeExit)))
                .then(literal("currentTimeMillis").executes(SystemCommand::executeCurrentTimeMillis))
        );
    }

    private static int executeGc(CommandContext<FabricClientCommandSource> context) {
        System.gc();
        return 1;
    }

    private static int executeExit(CommandContext<FabricClientCommandSource> context) {
        int status = IntegerArgumentType.getInteger(context, "status");
        System.exit(status);
        return status;
    }

    private static int executeCurrentTimeMillis(CommandContext<FabricClientCommandSource> context) {
        long time = System.currentTimeMillis();
        context.getSource().sendFeedback(Text.translatable("command.mdt.system.currentTimeMillis"));
        return (int) time;
    }
}
