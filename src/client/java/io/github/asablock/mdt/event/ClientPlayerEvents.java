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

package io.github.asablock.mdt.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public final class ClientPlayerEvents {
    private ClientPlayerEvents() {
    }

    public static final Event<PlayerLoaded> PLAYER_LOADED = EventFactory.createArrayBacked(PlayerLoaded.class, callbacks -> player -> {
        for (PlayerLoaded callback : callbacks) {
            callback.onPlayerLoaded(player);
        }
    });

    public static final Event<PlayerUnloading> PLAYER_UNLOADING = EventFactory.createArrayBacked(PlayerUnloading.class, callbacks -> player -> {
        for (PlayerUnloading callback : callbacks) {
            callback.onPlayerUnloading(player);
        }
    });

    @FunctionalInterface
    public interface PlayerLoaded {
        void onPlayerLoaded(AbstractClientPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerUnloading {
        void onPlayerUnloading(AbstractClientPlayerEntity player);
    }
}
