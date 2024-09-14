package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.asablock.mdt.IOUtil;
import io.github.asablock.mdt.Mdt;
import io.github.asablock.mdt.toggle.Toggle;
import io.github.asablock.mdt.toggle.ToggleSerializer;
import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ToggleCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> lab = literal("mtoggle");
        for (Toggle<?> toggle : Toggles.TOGGLES.values()) {
            LiteralArgumentBuilder<FabricClientCommandSource> l =
                    literal(toggle.getName()).executes(new ExecuteQuery(toggle))
                            .then(literal("reset").executes(new ExecuteReset<>(toggle)));
            toggle.appendCommandArgument(l, new ExecutesSet<>(toggle));
            lab.then(l);
        }
        lab.then(literal("resetall").executes(ToggleCommand::executeResetAll))
                .then(literal("reload").executes(ToggleCommand::executeReload))
                .then(literal("save").executes(ToggleCommand::executeSave));
        dispatcher.register(lab);
    }

    private record ExecutesSet<T>(Toggle<T> toggle) implements Toggle.Executes {
        @Override
        public Command<FabricClientCommandSource> withParentId(final int id) {
            return context -> {
                T value = toggle.getInputValue(context, id);
                T old = toggle.get();
                Boolean b = toggle.set(value);
                context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.set.success", toggle.getName(), toggle.toString(value), toggle.toString(old)));
                return b == null ? 0 : (b ? 2 : 1);
            };
        }
    }

    private record ExecuteQuery(Toggle<?> toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.query.success", toggle.getName(), toggle.valueToString()));
            return 1;
        }
    }

    private record ExecuteReset<T>(Toggle<T> toggle) implements Command<FabricClientCommandSource> {
        @Override
        public int run(CommandContext<FabricClientCommandSource> context) {
            T old = toggle.get();
            T to = toggle.defaultValue;
            Boolean b = toggle.set(to);
            context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.success", toggle.getName(), toggle.toString(to), toggle.toString(old)));
            return b == null ? 0 : (b ? 2 : 1);
        }
    }

    private static int executeResetAll(CommandContext<FabricClientCommandSource> context) {
        int mods = Toggles.resetAll();
        context.getSource().sendFeedback(Text.translatable("command.mdt.toggle.reset.all", mods));
        return mods;
    }

    private static int executeReload(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try (BufferedReader br = Files.newBufferedReader(Mdt.config)) {
            ToggleSerializer.readToggles(br);
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(IOUtil.getTextWriter(context.getSource()::sendError)));
            Mdt.LOGGER.error("Cannot read config", e);
            throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
        }
    }

    private static int executeSave(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try (BufferedWriter bw = Files.newBufferedWriter(Mdt.config)) {
            ToggleSerializer.saveToggles(bw);
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(IOUtil.getTextWriter(context.getSource()::sendError)));
            Mdt.LOGGER.error("Cannot save config", e);
            throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
        }
    }
}
