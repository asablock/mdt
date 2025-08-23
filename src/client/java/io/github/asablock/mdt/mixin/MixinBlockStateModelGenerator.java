/*
 * This file is part of mdt. mdt is a client-side mod for Minecraft.
 * Copyright (C) 2025  asablock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
