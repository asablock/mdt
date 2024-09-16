package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DisconnectCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mdisconnect").executes(DisconnectCommand::execute));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = context.getSource().getClient();
        if (client.world != null) {
            client.world.disconnect();
        }

        client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        client.setScreen(new TitleScreen());

        return 1;
    }
}
