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

import io.github.asablock.mdt.event.ClientPlayerEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class ClientLoadedPlayerManagerImpl {
    public static void load(Entity entity, ClientWorld world) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            ClientLoadedPlayerManager manager = (ClientLoadedPlayerManager) world;
            manager.mdt_addPlayer(player);
            ClientPlayerEvents.PLAYER_LOADED.invoker().onPlayerLoaded(player);
        }
    }

    public static void unload(Entity entity, ClientWorld world) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            ClientPlayerEvents.PLAYER_UNLOADING.invoker().onPlayerUnloading(player);
            ClientLoadedPlayerManager manager = (ClientLoadedPlayerManager) world;
            manager.mdt_removePlayer(player);
        }
    }
}
