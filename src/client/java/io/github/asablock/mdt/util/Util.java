package io.github.asablock.mdt.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class Util {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Util() {
    }

    public static void sendMessage(Text message) {
        client.inGameHud.getChatHud().addMessage(message);
        client.getNarratorManager().narrate(message);
    }
}
