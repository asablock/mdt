package io.github.asablock.mdt.mixin;

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

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void getRenderType(BlockState state, CallbackInfoReturnable<BlockRenderType> cir) {
        cir.setReturnValue(Toggles.showBarrier.get() ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        // from TranslucentBlock.java
        return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
    }
}
