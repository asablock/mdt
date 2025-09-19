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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.asablock.mdt.EnhancedKeyBindingHelper;
import io.github.asablock.mdt.MdtKeyBindings;
import io.github.asablock.mdt.util.Util;
import net.minecraft.component.Component;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Shadow @Final
    MergedComponentMap components;

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=item.components")))
    private boolean viewComponents(List<Text> instance, @Coerce Object o, Operation<Boolean> original) {
        if (MdtKeyBindings.VIEW_DATA_COMPONENTS_KEY.isPressed()) {
            for (Component<?> component : components) {
                instance.add(Util.toText(component));
            }
            return true;
        } else {
            return original.call(instance, o);
        }
    }

    @ModifyExpressionValue(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1))
    private MutableText componentsTextOverride(MutableText original, @Local int i) {
        if (EnhancedKeyBindingHelper.isBound(MdtKeyBindings.VIEW_DATA_COMPONENTS_KEY)) {
            return original.append(Text.translatable("mdt.view_data_components.press", MdtKeyBindings.VIEW_DATA_COMPONENTS_KEY.getBoundKeyLocalizedText()));
        } else {
            return original;
        }
    }
}
