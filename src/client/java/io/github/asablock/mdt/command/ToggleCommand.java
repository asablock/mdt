package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.Toggle;
import io.github.asablock.mdt.Toggles;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ToggleCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> lab = literal("mtoggle");
        for (Toggle value : Toggles.TOGGLES.values()) {
            lab.then(literal(value.name).executes(new ExecuteQuery(value))
                    .then(argument("value", BoolArgumentType.bool()).executes(new ExecuteSet(value))));
        }
        dispatcher.register(lab);
    }

    private record ExecuteSet(Toggle toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            boolean value = BoolArgumentType.getBool(context, "value");
            boolean old = toggle.enabled;
            toggle.enabled = value;
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.set.success", toggle.name, value, old));
            return old == value ? 0 : 1;
        }
    }

    private record ExecuteQuery(Toggle toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.query.success", toggle.name, toggle.enabled));
            return toggle.enabled ? 1 : 0;
        }
    }
}
