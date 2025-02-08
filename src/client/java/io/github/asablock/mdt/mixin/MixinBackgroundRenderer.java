package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.toggle.Toggles;
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
    @Redirect(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 1))
    private static boolean disableDarkness(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return !Toggles.disableDarkness.get() && instance.hasStatusEffect(effect);
    }

    @Redirect(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private static boolean disableNightVision(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return !Toggles.disableNightVision.get() && instance.hasStatusEffect(effect);
    }

    @Redirect(method = "method_42589", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;shouldApply(Lnet/minecraft/entity/LivingEntity;F)Z"))
    private static boolean disableBlindnessAndDarkness(BackgroundRenderer.StatusEffectFogModifier instance, LivingEntity entity, float tickDelta) {
        RegistryEntry<StatusEffect> effect = instance.getStatusEffect();
        if ((effect == StatusEffects.BLINDNESS && Toggles.disableBlindness.get())
                || (effect == StatusEffects.DARKNESS && Toggles.disableDarkness.get())) {
            return false;
        } else {
            return instance.shouldApply(entity, tickDelta);
        }
    }
}
