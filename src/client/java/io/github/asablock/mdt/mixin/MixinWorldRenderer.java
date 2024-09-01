package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Redirect(method = "hasBlindnessOrDarkness", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private boolean disableBlindness(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return !Toggles.disableBlindness.enabled && instance.hasStatusEffect(effect);
    }

    @Redirect(method = "hasBlindnessOrDarkness", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 1))
    private boolean disableDarkness(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return !Toggles.disableDarkness.enabled && instance.hasStatusEffect(effect);
    }
}
