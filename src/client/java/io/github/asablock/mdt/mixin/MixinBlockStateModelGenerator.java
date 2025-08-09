package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(BlockStateModelGenerator.class)
public abstract class MixinBlockStateModelGenerator {
    @Shadow public abstract void registerBuiltinWithParticle(Block block, Item particleSource);

    @Shadow public abstract void registerSimpleCubeAll(Block block);

    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/data/BlockStateModelGenerator;registerBuiltinWithParticle(Lnet/minecraft/block/Block;Lnet/minecraft/item/Item;)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/data/BlockStateModelGenerator;registerCaveVines()V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/data/BlockStateModelGenerator;registerLightBlock()V")))
    private void noBarrierParticle(BlockStateModelGenerator instance, Block block, Item particleSource, Operation<Void> original) {
        if (Toggles.showBarrier.get()) {
            registerSimpleCubeAll(block);
        } else {
            original.call(instance, block, particleSource);
        }
    }
}
