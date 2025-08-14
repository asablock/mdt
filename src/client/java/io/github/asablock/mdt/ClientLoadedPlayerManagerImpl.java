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
