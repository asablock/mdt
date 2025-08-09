package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager {
    @Shadow protected abstract float getDarknessFactor(float delta);

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/LightmapTextureManager;getDarknessFactor(F)F"))
    private float disableDarkness(LightmapTextureManager instance, float delta, Operation<Float> original) {
        return Toggles.disableDarkness.get() ? 0.0F : original.call(instance, delta);
    }

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private boolean disableNightVision(ClientPlayerEntity instance, RegistryEntry<StatusEffect> registryEntry, Operation<Boolean> original) {
        return !Toggles.disableNightVision.get() && original.call(instance, registryEntry);
    }
}
