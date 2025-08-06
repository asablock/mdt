package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.ClientLoadedPlayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements ClientLoadedPlayerManager {
    @Unique
    private List<AbstractClientPlayerEntity> mdt_loadedPlayers;

    @Unique
    private Map<UUID, AbstractClientPlayerEntity> mdt_playerMap;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initPlayerList(MinecraftClient client, ClientConnection clientConnection, ClientConnectionState clientConnectionState, CallbackInfo ci) {
        mdt_loadedPlayers = new ArrayList<>();
        mdt_playerMap = new HashMap<>();
    }

    @Override
    public @Nullable AbstractClientPlayerEntity mdt_getPlayer(String name) {
        for (AbstractClientPlayerEntity player : mdt_loadedPlayers) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
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
        return mdt_loadedPlayers;
    }

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    private void addRespawnPlayer(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        mdt_loadedPlayers.add(player);
        mdt_playerMap.put(player.getUuid(), player);
    }

    @Inject(method = "createEntity", at = @At("RETURN"))
    private void addOtherPlayer(EntitySpawnS2CPacket packet, CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() instanceof OtherClientPlayerEntity ocpe) {
            mdt_loadedPlayers.add(ocpe);
            mdt_playerMap.put(ocpe.getUuid(), ocpe);
        }
    }

    @WrapOperation(method = "method_64896", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;"))
    private Entity removePlayer(ClientWorld instance, int id, Operation<Entity> original) {
        Entity entity = original.call(instance, id);
        if (entity instanceof AbstractClientPlayerEntity) {
            mdt_loadedPlayers.remove(entity);
            mdt_playerMap.remove(entity.getUuid());
        }
        return entity;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void addJoinPlayer(GameJoinS2CPacket packet, CallbackInfo ci) {
        mdt_loadedPlayers.add(this.client.player);
        mdt_playerMap.put(this.client.player.getUuid(), this.client.player);
    }
}
