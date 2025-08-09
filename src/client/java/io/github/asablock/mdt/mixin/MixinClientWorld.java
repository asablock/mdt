package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {
    @WrapOperation(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean noBarrierParticleRender(Set<Item> instance, Object o, Operation<Boolean> original) {
        Item item = (Item) o;
        if (item == Items.BARRIER) {
            return !Toggles.showBarrier.get();
        } else {
            return original.call(instance, o);
        }
    }
}
