package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.command.argument.PEnumArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class FormattedChatCommand {
    public static final StringBuilder STRING_BUILDER = new StringBuilder();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mformattedchat")
                .then(literal("append").then(argument("chat", StringArgumentType.greedyString()).executes(FormattedChatCommand::executeAppend)))
                .then(literal("preview").executes(FormattedChatCommand::executePreview))
                .then(literal("addformat").then(argument("format", new PEnumArgumentType<>(Formatting.class)).executes(FormattedChatCommand::executeAddFormat)))
                .then(literal("send").executes(FormattedChatCommand::executeSend))
                .then(literal("clear").executes(FormattedChatCommand::executeClear))
        );
    }

    public static int executeAppend(CommandContext<FabricClientCommandSource> context) {
        String chat = StringArgumentType.getString(context, "chat");
        STRING_BUILDER.append(chat);
        return 1;
    }

    public static int executePreview(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal(STRING_BUILDER.toString()));
        return 1;
    }

    public static int executeAddFormat(CommandContext<FabricClientCommandSource> context) {
        Formatting formatting = context.getArgument("format", Formatting.class);
        STRING_BUILDER.append(formatting);
        return 1;
    }

    public static int executeSend(CommandContext<FabricClientCommandSource> context) {
        context.getSource().getClient().getNetworkHandler().sendChatMessage(STRING_BUILDER.toString());
        STRING_BUILDER.setLength(0);
        return 1;
    }

    public static int executeClear(CommandContext<FabricClientCommandSource> context) {
        STRING_BUILDER.setLength(0);
        return 1;
    }
}
