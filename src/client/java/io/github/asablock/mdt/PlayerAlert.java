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

package io.github.asablock.mdt;

import io.github.asablock.mdt.toggle.Toggles;
import io.github.asablock.mdt.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

public class PlayerAlert {
    public static double squaredAlertRadius = 0.0;
    private static final Set<AbstractClientPlayerEntity> TRACKED_PLAYERS = new HashSet<>();

    public static void tick(MinecraftClient client) {
        if (!Toggles.playerAlert_enabled.get()) return;
        ClientLoadedPlayerManager manager = (ClientLoadedPlayerManager) client.world;
        if (manager == null) return;
        for (AbstractClientPlayerEntity player : manager.mdt_getPlayerList()) {
            if (player == client.player && !Toggles.playerAlert_alertSelf.get()) continue;

            double squaredDistance = player.squaredDistanceTo(client.player);
            if (squaredDistance > squaredAlertRadius) {
                if (TRACKED_PLAYERS.remove(player)) {
                    alertLeave(player);
                }
            } else {
                if (TRACKED_PLAYERS.add(player)) {
                    alert(player);
                }
            }
        }
    }

    public static void clear() {
        TRACKED_PLAYERS.clear();
    }

    public static void afterWorldChange(MinecraftClient client, ClientWorld world) {
        if (!Toggles.playerAlert_enabled.get()) return;
        TRACKED_PLAYERS.clear();
        Util.sendMessage(Text.translatable("mdt.playerAlert.worldChanged").formatted(Formatting.YELLOW));
    }

    public static void playerUnloading(AbstractClientPlayerEntity player) {
        if (!Toggles.playerAlert_enabled.get()) return;
        if (TRACKED_PLAYERS.remove(player)) {
            alertLeave(player);
        }
    }

    private static void alert(AbstractClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        String formattedDistance = format(player.distanceTo(client.player));
        Util.sendMessage(Text.translatable("mdt.playerAlert.alert", player.getDisplayName(), formattedDistance, format(player.getX()), format(player.getY()), format(player.getZ())).formatted(Formatting.RED));
        if (Toggles.playerAlert_sound.get()) {
            client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER);
        }
    }

    private static void alertLeave(AbstractClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        String formattedDistance = format(player.distanceTo(client.player));
        Util.sendMessage(Text.translatable("mdt.playerAlert.alertLeave", player.getDisplayName(), formattedDistance, format(player.getX()), format(player.getY()), format(player.getZ())).formatted(Formatting.YELLOW));
        if (Toggles.playerAlert_sound.get()) {
            client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER);
        }
    }

    private static String format(double d) {
        return String.format("%.2f", d);
    }
}
