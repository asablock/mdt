package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.toggle.Toggle;
import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ToggleCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> lab = literal("mtoggle");
        for (Toggle<?> toggle : Toggles.TOGGLES.values()) {
            lab.then(literal(toggle.name).executes(new ExecuteQuery(toggle))
                    .then(literal("reset").executes(new ExecuteReset<>(toggle)))
                    .then(literal("set").then(toggle.createCommandNode(new ExecuteSet<>(toggle)))));
        }
        lab.then(literal("resetall").executes(ToggleCommand::resetAll));
        dispatcher.register(lab);
    }

    private record ExecuteSet<T>(Toggle<T> toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            T value = toggle.getInputValue(context);
            T old = toggle.get();
            Boolean b = toggle.set(value);
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.set.success", toggle.name, toggle.toString(value), toggle.toString(old)));
            return b == null ? 0 : (b ? 2 : 1);
        }
    }

    private record ExecuteQuery(Toggle<?> toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.query.success", toggle.name, toggle.valueToString()));
            return 1;
        }
    }

    private record ExecuteReset<T>(Toggle<T> toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            T old = toggle.get();
            T to = toggle.defaultValue;
            Boolean b = toggle.set(to);
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.success", toggle.name, toggle.toString(to), toggle.toString(old)));
            return b == null ? 0 : (b ? 2 : 1);
        }
    }

    private static int resetAll(CommandContext<FabricClientCommandSource> context) {
        for (Toggle<?> value : Toggles.TOGGLES.values()) {
            value.reset();
        }
        context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.all"));
        return 1;
    }
}
