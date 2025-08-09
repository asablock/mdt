package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.ClientLoadedPlayerManager;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements ClientLoadedPlayerManager {
    @Unique
    private List<AbstractClientPlayerEntity> mdt_playerList;

    @Unique
    private Map<UUID, AbstractClientPlayerEntity> mdt_playerMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initFields(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionType, int loadDistance, int simulationDistance, WorldRenderer worldRenderer, boolean debugWorld, long seed, int seaLevel, CallbackInfo ci) {
        mdt_playerList = new ArrayList<>();
        mdt_playerMap = new HashMap<>();
    }

    @WrapOperation(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean noBarrierParticleRender(Set<Item> instance, Object o, Operation<Boolean> original) {
        Item item = (Item) o;
        if (item == Items.BARRIER) {
            return !Toggles.showBarrier.get();
        } else {
            return original.call(instance, o);
        }
    }

    @Override
    public @Nullable AbstractClientPlayerEntity mdt_getPlayer(String name) {
        for (AbstractClientPlayerEntity player : mdt_playerList) {
            if (name.equals(player.getGameProfile().getName())) {
                return player;
            }
        }
        return null;
    }

    @Override
    public @Nullable AbstractClientPlayerEntity mdt_getPlayer(UUID uuid) {
        return mdt_playerMap.get(uuid);
    }

    @Override
    public List<AbstractClientPlayerEntity> mdt_getPlayerList() {
        return mdt_playerList;
    }

    @Override
    public boolean mdt_addPlayer(AbstractClientPlayerEntity player) {
        if (!mdt_playerList.contains(player)) {
            mdt_playerList.add(player);
            mdt_playerMap.put(player.getUuid(), player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mdt_removePlayer(AbstractClientPlayerEntity player) {
        if (mdt_playerList.contains(player)) {
            mdt_playerList.remove(player);
            mdt_playerMap.remove(player.getUuid(), player);
            return true;
        } else {
            return false;
        }
    }
}
