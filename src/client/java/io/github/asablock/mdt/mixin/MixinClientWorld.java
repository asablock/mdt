package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Redirect(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean noBarrierParticleRender(Set<Item> instance, Object o) {
        Item item = (Item) o;
        if (item == Items.BARRIER) {
            return !Toggles.showBarrier.get();
        } else {
            return instance.contains(item);
        }
    }
}
