package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean disableBlindness(ClientPlayerEntity instance, RegistryEntry<StatusEffect> registryEntry) {
        return !Toggles.disableBlindness.enabled && instance.hasStatusEffect(registryEntry);
    }
}
