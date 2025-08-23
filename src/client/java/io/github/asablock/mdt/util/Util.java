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

package io.github.asablock.mdt.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public final class Util {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Util() {
    }

    public static void sendMessage(Text message) {
        client.inGameHud.getChatHud().addMessage(message);
        client.getNarratorManager().narrate(message);
    }

    public static Text swingSourceToText(ActionResult.SwingSource swingSource) {
        return Text.translatable(switch (swingSource) {
            case NONE -> "mdt.none";
            case CLIENT -> "mdt.client";
            case SERVER -> "mdt.server";
        });
    }

    public static Text actionResultToText(ActionResult actionResult) {
        switch (actionResult) {
            case ActionResult.Fail ignored -> {
                return Text.translatable("mdt.actionResult.fail");
            }
            case ActionResult.Pass ignored -> {
                return Text.translatable("mdt.actionResult.pass");
            }
            case ActionResult.PassToDefaultBlockAction ignored -> {
                return Text.translatable("mdt.actionResult.passToDefaultBlockAction");
            }
            case ActionResult.Success success -> {
                ActionResult.ItemContext itemContext = success.itemContext();
                ItemStack stack = itemContext.newHandStack();
                return Text.translatable(
                        "mdt.actionResult.success",
                        swingSourceToText(success.swingSource()),
                        itemContext.incrementStat(),
                        stack != null ? stack.toHoverableText() : null
                );
            }
        }
    }
}
