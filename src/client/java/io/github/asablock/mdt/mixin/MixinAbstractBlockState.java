package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
    @Shadow public abstract boolean isOf(Block block);

    @WrapMethod(method = "shouldBlockVision")
    private boolean barrierBlockVision(BlockView world, BlockPos pos, Operation<Boolean> original) {
        return (!isOf(Blocks.BARRIER) || !Toggles.showBarrier.get()) && original.call(world, pos);
    }
}
