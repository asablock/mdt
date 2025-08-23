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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrierBlock.class)
public abstract class MixinBarrierBlock extends Block {
    public MixinBarrierBlock(Settings settings) {
        super(settings);
    }

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private BlockRenderType getRenderType(BlockRenderType original) {
        return Toggles.showBarrier.get() ? BlockRenderType.MODEL : original;
    }

    @Override // @WrapMethod cannot be used here
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        // from TranslucentBlock.java
        return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
    }
}
