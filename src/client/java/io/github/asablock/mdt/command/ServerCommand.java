package io.github.asablock.mdt.command;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ServerCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mserver")
                .then(literal("ip").executes(ServerCommand::executeIp))
                .then(literal("playerproperties").then(argument("name", StringArgumentType.word()).executes(ServerCommand::executePlayerProfile))));
    }

    public static int executeIp(CommandContext<FabricClientCommandSource> context) {
        ClientPlayNetworkHandler cpnh = context.getSource().getClient().getNetworkHandler();
        if (cpnh != null) {
            ServerInfo serverInfo = cpnh.getServerInfo();
            if (serverInfo != null) {
                context.getSource().sendFeedback(Text.translatable("command.mdt.server.ip.success", serverInfo.address));
                return 1;
            } else {
                context.getSource().sendError(Text.translatable("command.mdt.server.ip.null_server_info"));
            }
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.server.not_in_game"));
        }
        return 0;
    }

    public static int executePlayerProfile(CommandContext<FabricClientCommandSource> context) {
        ClientPlayNetworkHandler cpnh = context.getSource().getClient().getNetworkHandler();
        if (cpnh != null) {
            String name = StringArgumentType.getString(context, "name");
            PlayerListEntry playerListEntry = cpnh.getPlayerListEntry(name);
            if (playerListEntry != null) {
                GameProfile profile = playerListEntry.getProfile();
                PropertyMap map = profile.getProperties();
                int count = map.size();
                context.getSource().sendFeedback(Text.translatable("command.mdt.server.playerproperties.count", count, name));
                for (Map.Entry<String, Property> entry : map.entries()) {
                    Property property = entry.getValue();
                    context.getSource().sendFeedback(Text.translatable("command.mdt.server.playerproperties.line", entry.getKey(), property.name(), property.value(), property.signature()));
                }
                return count;
            } else {
                context.getSource().sendError(Text.translatable("command.mdt.server.player_not_exist", name));
            }
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.server.not_in_game"));
        }
        return 0;
    }
}
