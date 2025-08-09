package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @WrapOperation(method = "hasBlindnessOrDarkness", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private boolean disableBlindness(LivingEntity instance, RegistryEntry<StatusEffect> effect, Operation<Boolean> original) {
        return !Toggles.disableBlindness.get() && original.call(instance, effect);
    }

    @WrapOperation(method = "hasBlindnessOrDarkness", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 1))
    private boolean disableDarkness(LivingEntity instance, RegistryEntry<StatusEffect> effect, Operation<Boolean> original) {
        return !Toggles.disableDarkness.get() && original.call(instance, effect);
    }
}
