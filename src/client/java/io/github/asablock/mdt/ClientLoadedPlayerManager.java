package io.github.asablock.mdt;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ClientLoadedPlayerManager {
    @Nullable
    AbstractClientPlayerEntity mdt_getPlayer(String name);

    @Nullable
    AbstractClientPlayerEntity mdt_getPlayer(UUID uuid);

    List<AbstractClientPlayerEntity> mdt_getPlayerList();

    boolean mdt_addPlayer(AbstractClientPlayerEntity player);

    boolean mdt_removePlayer(AbstractClientPlayerEntity player);
}
