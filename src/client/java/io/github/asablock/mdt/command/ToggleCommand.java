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
        for (Toggle toggle : Toggles.TOGGLES.values()) {
            lab.then(literal(toggle.name).executes(new ExecuteQuery(toggle))
                    .then(argument("value", BoolArgumentType.bool()).executes(new ExecuteSet(toggle)))
                    .then(literal("reset").executes(new ExecuteReset(toggle))));
        }
        lab.then(literal("reset").executes(ToggleCommand::resetAll));
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

    private record ExecuteReset(Toggle toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            boolean old = toggle.enabled;
            boolean changed = toggle.reset();
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.success", toggle.name, toggle.enabled, old));
            return changed ? 1 : 0;
        }
    }

    private static int resetAll(CommandContext<FabricClientCommandSource> context) {
        for (Toggle value : Toggles.TOGGLES.values()) {
            value.reset();
        }
        context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.all"));
        return 1;
    }
}
