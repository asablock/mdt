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
            case NONE -> "none";
            case CLIENT -> "client";
            case SERVER -> "server";
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
