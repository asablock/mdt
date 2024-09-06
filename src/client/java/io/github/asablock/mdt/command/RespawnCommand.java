package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class RespawnCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mrespawn").executes(RespawnCommand::execute));
    }

    public static int execute(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getClient().player;
        if (player != null) {
            player.requestRespawn();
            return 1;
        } else {
            return 0;
        }
    }
}
