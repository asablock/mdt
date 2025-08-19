package io.github.asablock.mdt;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.asablock.mdt.command.*;
import io.github.asablock.mdt.command.argument.ClientEntitySelectorOptions;
import io.github.asablock.mdt.event.ClientPlayerEvents;
import io.github.asablock.mdt.toggle.ToggleSerializer;
import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Mdt implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Minecraft Client Debug Toolkit");
	public static final SimpleCommandExceptionType LINE_SEPARATOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.mdt.lineSeparator"));
	public static Path config;

	public static final PrintStream LOGGER_OUT_PRINT_STREAM = System.out;
	public static final PrintStream LOGGER_ERROR_PRINT_STREAM = System.err;
	public static final InputStream SYSIN = System.in;
	// These are initialized a little bit earlier than config loading (replaces them)

	@Override
	public void onInitializeClient() {
		// Initialize toggles
		Toggles.init();

		config = FabricLoader.getInstance().getConfigDir().resolve("mdt.json");
		// Load config
		if (Files.exists(config)) {
            try (BufferedReader br = Files.newBufferedReader(config)) {
                ToggleSerializer.readToggles(br);
            } catch (Exception e) {
                LOGGER.error("Cannot read config", e);
            }
        }

		// Redirect System.in
		System.setIn(SystemCommand.SYSIN);

		// Register ClientEntitySelectorOptions
		ClientEntitySelectorOptions.register();

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			SendChatCommand.register(dispatcher);
			ToggleCommand.register(dispatcher);
			RespawnCommand.register(dispatcher);
			DisconnectCommand.register(dispatcher);
			SystemCommand.register(dispatcher);
			ServerCommand.register(dispatcher);
			InteractCommand.register(dispatcher);
			DebugCommand.register(dispatcher);
			ScoreboardCommand.register(dispatcher);
			FormattedChatCommand.register(dispatcher);
			MdtCommand.register(dispatcher);
		});

		// Save config on client stop
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try (BufferedWriter bw = Files.newBufferedWriter(config)) {
                ToggleSerializer.saveToggles(bw);
            } catch (Exception e) {
                LOGGER.error("Cannot save config", e);
            }
        });

		// barrier: cutout
		BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getCutout());

		ClientTickEvents.END_CLIENT_TICK.register(PlayerAlert::tick);

		ClientEntityEvents.ENTITY_LOAD.register(ClientLoadedPlayerManagerImpl::load);
		ClientEntityEvents.ENTITY_UNLOAD.register(ClientLoadedPlayerManagerImpl::unload);

		ClientPlayerEvents.PLAYER_UNLOADING.register(PlayerAlert::playerUnloading);

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(PlayerAlert::afterWorldChange);
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object o) {
		return (T) o;
	}

	public static GameMode getGameMode(MinecraftClient client, AbstractClientPlayerEntity player) {
		return client.getNetworkHandler().getPlayerListEntry(player.getUuid()).getGameMode();
	}
}