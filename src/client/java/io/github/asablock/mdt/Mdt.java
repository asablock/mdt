package io.github.asablock.mdt;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.asablock.mdt.command.*;
import io.github.asablock.mdt.toggle.ToggleSerializer;
import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Mdt implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Minecraft Client Debug Toolkit");
	public static final SimpleCommandExceptionType LINE_SEPARATOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.mdt.lineSeparator"));
	public static Path config;

	@Override
	public void onInitializeClient() {
		Toggles.init();

		config = FabricLoader.getInstance().getConfigDir().resolve("mdt.json");
		if (Files.exists(config)) {
            try (BufferedReader br = Files.newBufferedReader(config)) {
                ToggleSerializer.readToggles(br);
            } catch (Exception e) {
                LOGGER.error("Cannot read config", e);
            }
        }

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			SendChatCommand.register(dispatcher);
			ToggleCommand.register(dispatcher);
			RespawnCommand.register(dispatcher);
			DisconnectCommand.register(dispatcher);
			SystemCommand.register(dispatcher);
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try (BufferedWriter bw = Files.newBufferedWriter(config)) {
                ToggleSerializer.saveToggles(bw);
            } catch (Exception e) {
                LOGGER.error("Cannot save config", e);
            }
        });
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object o) {
		return (T) o;
	}
}