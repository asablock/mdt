package io.github.asablock.mdt.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;

public final class ClientPlayerEvents {
    private ClientPlayerEvents() {
    }

    public static final Event<PlayerRespawned> PLAYER_RESPAWNED = EventFactory.createArrayBacked(PlayerRespawned.class, callbacks -> player -> {
        for (PlayerRespawned callback : callbacks) {
            callback.onPlayerRespawned(player);
        }
    });

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

    public static final Event<PlayerJoined> PLAYER_JOINED = EventFactory.createArrayBacked(PlayerJoined.class, callbacks -> player -> {
        for (PlayerJoined callback : callbacks) {
            callback.onPlayerJoined(player);
        }
    });

    @FunctionalInterface
    public interface PlayerRespawned {
        void onPlayerRespawned(ClientPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerLoaded {
        void onPlayerLoaded(OtherClientPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerUnloading {
        void onPlayerUnloading(AbstractClientPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerJoined {
        void onPlayerJoined(ClientPlayerEntity player);
    }
}
