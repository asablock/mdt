package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {
    // I know this really sucks
    @Inject(method = "shouldBlockVision", at = @At("HEAD"), cancellable = true)
    private void barrierBlockVision(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AbstractBlock.AbstractBlockState self = (AbstractBlock.AbstractBlockState) (Object) this;
        if (self.isOf(Blocks.BARRIER) && Toggles.showBarrier.get()) {
            cir.setReturnValue(false);
        }
    }
}
