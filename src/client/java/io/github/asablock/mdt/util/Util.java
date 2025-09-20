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

import com.mojang.serialization.DataResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Objects;

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

    public static void sendChat(String chatText, boolean addToHistory) {
        if (!chatText.isEmpty()) {
            if (addToHistory) {
                client.inGameHud.getChatHud().addToMessageHistory(chatText);
            }

            if (chatText.startsWith("/")) {
                client.player.networkHandler.sendChatCommand(chatText.substring(1));
            } else {
                client.player.networkHandler.sendChatMessage(chatText);
            }
        }
    }

    public static ActionResult applyHitResult(HitResult hitResult) {
        if (hitResult != null) {
            for (Hand hand : Hand.values()) {
                ItemStack itemStack = client.player.getStackInHand(hand);
                switch (hitResult.getType()) {
                    case ENTITY:
                        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                        Entity entity = entityHitResult.getEntity();
                        ActionResult actionResult = client.interactionManager.interactEntityAtLocation(client.player, entity, entityHitResult, hand);
                        if (!actionResult.isAccepted()) {
                            actionResult = client.interactionManager.interactEntity(client.player, entity, hand);
                        }

                        if (actionResult instanceof ActionResult.Success success) {
                            if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                                client.player.swingHand(hand);
                            }
                            return actionResult;
                        }
                        break;
                    case BLOCK:
                        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                        int i = itemStack.getCount();
                        ActionResult actionResult2 = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
                        if (actionResult2 instanceof ActionResult.Success success2) {
                            if (success2.swingSource() == ActionResult.SwingSource.CLIENT) {
                                client.player.swingHand(hand);
                                if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.interactionManager.hasCreativeInventory())) {
                                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                }
                            }
                            return actionResult2;
                        }

                        if (actionResult2 instanceof ActionResult.Fail) {
                            return actionResult2;
                        }
                }

                if (!itemStack.isEmpty() && client.interactionManager.interactItem(client.player, hand) instanceof ActionResult.Success success3) {
                    if (success3.swingSource() == ActionResult.SwingSource.CLIENT) {
                        client.player.swingHand(hand);
                    }

                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                    return success3;
                }
            }
        }
        return null;
    }

    public static <T> MutableText toText(Component<T> component) {
        ComponentType<T> type = component.type();
        Identifier id = Registries.DATA_COMPONENT_TYPE.getId(type);
        MutableText idText = id != null ? Text.literal(id.toString()).formatted(Formatting.GREEN) : Text.translatable("mdt.view_data_components.unknown").formatted(Formatting.RED);
        DataResult<Text> dataResult = component.encode(Objects.requireNonNull(client.world).getRegistryManager().getOps(NbtOps.INSTANCE)).map(NbtHelper::toPrettyPrintedText);
        Text nbt;
        if (dataResult.isSuccess()) {
            nbt = dataResult.getOrThrow();
        } else {
            nbt = Text.translatable("mdt.view_data_components.not_encodable", dataResult.error().orElseThrow().message()).formatted(Formatting.RED);
        }
        return idText.append(Text.literal(": ").formatted(Formatting.WHITE)).append(nbt);
    }
}
