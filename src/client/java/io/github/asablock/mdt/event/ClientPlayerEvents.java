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
