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
