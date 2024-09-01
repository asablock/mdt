package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 1))
    private static boolean disableDarkness(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return !Toggles.disableDarkness.enabled && instance.hasStatusEffect(effect);
    }

    @Redirect(method = "method_42589", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;shouldApply(Lnet/minecraft/entity/LivingEntity;F)Z"))
    private static boolean disableBlindnessAndDarkness(BackgroundRenderer.StatusEffectFogModifier instance, LivingEntity entity, float tickDelta) {
        RegistryEntry<StatusEffect> effect = instance.getStatusEffect();
        if ((effect == StatusEffects.BLINDNESS && Toggles.disableBlindness.enabled)
                || (effect == StatusEffects.DARKNESS && Toggles.disableDarkness.enabled)) {
            return false;
        } else {
            return instance.shouldApply(entity, tickDelta);
        }
    }
}
