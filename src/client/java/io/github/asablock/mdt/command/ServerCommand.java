/*
 * This file is part of mdt. mdt is a client-side mod for Minecraft.
 * Copyright (C) 2025  asablock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.asablock.mdt.command;

import com.google.common.hash.HashCode;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.mixin.ServerResourcePackLoaderAccessor;
import io.github.asablock.mdt.mixin.ServerResourcePackManagerAccessor;
import io.github.asablock.mdt.mixin.ServerResourcePackManagerPackEntryAccessor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ServerCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mserver")
                .then(literal("address").executes(ServerCommand::executeAddress))
                .then(literal("playerproperties").then(argument("name", StringArgumentType.word()).executes(ServerCommand::executePlayerProfile)))
                .then(literal("resourcepacks")
                        .then(literal("list").executes(ServerCommand::executeResourcePacksList))
                        .then(literal("path").then(argument("id", UuidArgumentType.uuid()).executes(ServerCommand::executeResourcePacksPath)))
                )
        );
    }

    public static int executeAddress(CommandContext<FabricClientCommandSource> context) {
        ClientPlayNetworkHandler cpnh = context.getSource().getClient().getNetworkHandler();
        if (cpnh != null) {
            ServerInfo serverInfo = cpnh.getServerInfo();
            if (serverInfo != null) {
                context.getSource().sendFeedback(Text.translatable("command.mdt.server.address.success", serverInfo.address));
                return Command.SINGLE_SUCCESS;
            } else {
                context.getSource().sendError(Text.translatable("command.mdt.server.address.null_server_info"));
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

    public static int executeResourcePacksList(CommandContext<FabricClientCommandSource> context) {
        ServerResourcePackManager packManager = ((ServerResourcePackLoaderAccessor) context.getSource().getClient().getServerResourcePackProvider()).getManager();
        List<ServerResourcePackManager.PackEntry> packs = ((ServerResourcePackManagerAccessor) packManager).getPacks();
        context.getSource().sendFeedback(Text.translatable("command.mdt.server.resourcepacks.list.count", packs.size()));
        for (ServerResourcePackManager.PackEntry pack : packs) {
            ServerResourcePackManagerPackEntryAccessor accessor = (ServerResourcePackManagerPackEntryAccessor) pack;
            UUID id = accessor.getId();
            HashCode hashCode = accessor.getHashCode();
            Path path = accessor.getPath();
            Text pathText = Text.literal(path.toString())
                    .formatted(Formatting.UNDERLINE)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toAbsolutePath().toString())));
            context.getSource().sendFeedback(Text.translatable("command.mdt.server.resourcepacks.list.line", id, hashCode, pathText));
        }
        return packs.size();
    }

    public static int executeResourcePacksPath(CommandContext<FabricClientCommandSource> context) {
        UUID uuid = context.getArgument("id", UUID.class);
        ServerResourcePackManager packManager = ((ServerResourcePackLoaderAccessor) context.getSource().getClient().getServerResourcePackProvider()).getManager();
        ServerResourcePackManager.PackEntry pack = ((ServerResourcePackManagerAccessor) packManager).invokeGet(uuid);
        if (pack != null) {
            ServerResourcePackManagerPackEntryAccessor accessor = (ServerResourcePackManagerPackEntryAccessor) pack;
            UUID id = accessor.getId();
            HashCode hashCode = accessor.getHashCode();
            Path path = accessor.getPath();
            Text pathText = Text.literal(path.toString())
                    .formatted(Formatting.UNDERLINE)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toAbsolutePath().toString())));
            context.getSource().sendFeedback(Text.translatable("command.mdt.server.resourcepacks.list.line", id, hashCode, pathText));
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.server.resourcepacks.path.not_found"));
            return 0;
        }
    }
}
