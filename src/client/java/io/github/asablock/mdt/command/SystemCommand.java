package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.util.IOUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SystemCommand {
    private static final IOUtil.StringBufferReader SYSIN_CHAT_READER = new IOUtil.StringBufferReader();
    public static final InputStream SYSIN = IOUtil.suppress(ReaderInputStream.builder().setCharset(StandardCharsets.UTF_8).setReader(SYSIN_CHAT_READER)::get);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("msystem")
                .then(literal("gc").executes(SystemCommand::executeGc))
                .then(literal("exit").then(argument("status", IntegerArgumentType.integer()).executes(SystemCommand::executeExit)))
                .then(literal("currentTimeMillis").executes(SystemCommand::executeCurrentTimeMillis))
                .then(literal("in").then(argument("input", StringArgumentType.greedyString()).executes(SystemCommand::executesIn)))
                .then(literal("out.println").then(argument("output", StringArgumentType.greedyString()).executes(SystemCommand::executesOutPrintln)))
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

    private static int executesIn(CommandContext<FabricClientCommandSource> context) {
        String input = StringArgumentType.getString(context, "input");
        SYSIN_CHAT_READER.append(input + '\n');
        return 1;
    }

    private static int executesOutPrintln(CommandContext<FabricClientCommandSource> context) {
        String output = StringArgumentType.getString(context, "output");
        System.out.println(output);
        return 1;
    }
}
