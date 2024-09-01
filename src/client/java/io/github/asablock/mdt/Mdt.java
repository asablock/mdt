package io.github.asablock.mdt;

import io.github.asablock.mdt.command.SendChatCommand;
import io.github.asablock.mdt.command.ToggleCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class Mdt implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			SendChatCommand.register(dispatcher);
			ToggleCommand.register(dispatcher);
		});
	}
}