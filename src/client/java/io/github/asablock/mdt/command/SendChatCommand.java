package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.mixin.ClientPlayNetworkHandlerAccessor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.time.Instant;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SendChatCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("msendchat").then(argument("content", StringArgumentType.greedyString()).executes(SendChatCommand::execute)));
    }

    public static int execute(CommandContext<FabricClientCommandSource> context) {
        String content = StringArgumentType.getString(context, "content");
        ClientPlayNetworkHandler handler = context.getSource().getClient().player.networkHandler;
        ClientPlayNetworkHandlerAccessor accessor = (ClientPlayNetworkHandlerAccessor) handler;

        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = accessor.getLastSeenMessagesCollector().collect();
        MessageSignatureData messageSignatureData = accessor.getMessagePacker().pack(new MessageBody(content, instant, l, lastSeenMessages.lastSeen()));
        handler.sendPacket(new ChatMessageC2SPacket(content, instant, l, messageSignatureData, lastSeenMessages.update()));
        return 1;
    }
}
