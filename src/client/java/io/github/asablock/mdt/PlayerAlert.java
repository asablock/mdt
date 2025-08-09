package io.github.asablock.mdt;

import io.github.asablock.mdt.toggle.Toggles;
import io.github.asablock.mdt.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

public class PlayerAlert {
    public static double squaredAlertRadius = 0.0;
    private static final Set<AbstractClientPlayerEntity> TRACKED_PLAYERS = new HashSet<>();

    public static void tick(MinecraftClient client) {
        ClientLoadedPlayerManager manager = (ClientLoadedPlayerManager) client.world;
        if (manager == null) return;
        for (AbstractClientPlayerEntity player : manager.mdt_getPlayerList()) {
            if (player == client.player && !Toggles.playerAlert_alertSelf.get()) continue;

            double squaredDistance = player.squaredDistanceTo(client.player);
            if (squaredDistance > squaredAlertRadius) {
                if (TRACKED_PLAYERS.remove(player)) {
                    alertLeave(player);
                }
            } else {
                if (TRACKED_PLAYERS.add(player)) {
                    alert(player);
                }
            }
        }
    }

    public static void afterWorldChange(MinecraftClient client, ClientWorld world) {
        TRACKED_PLAYERS.clear();
        Util.sendMessage(Text.translatable("mdt.playerAlert.worldChanged").formatted(Formatting.YELLOW));
    }

    public static void playerUnloading(AbstractClientPlayerEntity player) {
        if (TRACKED_PLAYERS.remove(player)) {
            alertLeave(player);
        }
    }

    private static void alert(AbstractClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        String formattedDistance = format(player.distanceTo(client.player));
        Util.sendMessage(Text.translatable("mdt.playerAlert.alert", player.getDisplayName(), formattedDistance, format(player.getX()), format(player.getY()), format(player.getZ())).formatted(Formatting.RED));
        if (Toggles.playerAlert_sound.get()) {
            client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER);
        }
    }

    private static void alertLeave(AbstractClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        String formattedDistance = format(player.distanceTo(client.player));
        Util.sendMessage(Text.translatable("mdt.playerAlert.alertLeave", player.getDisplayName(), formattedDistance, format(player.getX()), format(player.getY()), format(player.getZ())).formatted(Formatting.YELLOW));
        if (Toggles.playerAlert_sound.get()) {
            client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER);
        }
    }

    private static String format(double d) {
        return String.format("%.2f", d);
    }
}
