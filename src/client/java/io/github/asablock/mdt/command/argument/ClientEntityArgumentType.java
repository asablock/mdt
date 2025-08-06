package io.github.asablock.mdt.command.argument;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientEntityArgumentType implements ArgumentType<ClientEntitySelector> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
    public static final SimpleCommandExceptionType TOO_MANY_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.toomany"));
    public static final SimpleCommandExceptionType TOO_MANY_PLAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.toomany"));
    public static final SimpleCommandExceptionType PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.player.entities")
    );
    public static final SimpleCommandExceptionType ENTITY_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.entity.notfound.entity")
    );
    public static final SimpleCommandExceptionType PLAYER_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.entity.notfound.player")
    );
    final boolean singleTarget;
    final boolean playersOnly;

    protected ClientEntityArgumentType(boolean singleTarget, boolean playersOnly) {
        this.singleTarget = singleTarget;
        this.playersOnly = playersOnly;
    }

    public static ClientEntityArgumentType entity() {
        return new ClientEntityArgumentType(true, false);
    }

    public static Entity getEntity(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ClientEntitySelector.class).getEntity(context.getSource());
    }

    public static ClientEntityArgumentType entities() {
        return new ClientEntityArgumentType(false, false);
    }

    public static Collection<? extends Entity> getEntities(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        Collection<? extends Entity> collection = getOptionalEntities(context, name);
        if (collection.isEmpty()) {
            throw ENTITY_NOT_FOUND_EXCEPTION.create();
        } else {
            return collection;
        }
    }

    public static Collection<? extends Entity> getOptionalEntities(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ClientEntitySelector.class).getEntities(context.getSource());
    }

    public static Collection<AbstractClientPlayerEntity> getOptionalPlayers(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ClientEntitySelector.class).getPlayers(context.getSource());
    }

    public static ClientEntityArgumentType player() {
        return new ClientEntityArgumentType(true, true);
    }

    public static AbstractClientPlayerEntity getPlayer(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ClientEntitySelector.class).getPlayer(context.getSource());
    }

    public static ClientEntityArgumentType players() {
        return new ClientEntityArgumentType(false, true);
    }

    public static Collection<AbstractClientPlayerEntity> getPlayers(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        List<AbstractClientPlayerEntity> list = context.getArgument(name, ClientEntitySelector.class).getPlayers(context.getSource());
        if (list.isEmpty()) {
            throw PLAYER_NOT_FOUND_EXCEPTION.create();
        } else {
            return list;
        }
    }

    public ClientEntitySelector parse(StringReader reader) throws CommandSyntaxException {
        ClientEntitySelectorReader entitySelectorReader = new ClientEntitySelectorReader(reader);
        ClientEntitySelector entitySelector = entitySelectorReader.read();
        if (entitySelector.getLimit() > 1 && this.singleTarget) {
            if (this.playersOnly) {
                reader.setCursor(0);
                throw TOO_MANY_PLAYERS_EXCEPTION.createWithContext(reader);
            } else {
                reader.setCursor(0);
                throw TOO_MANY_ENTITIES_EXCEPTION.createWithContext(reader);
            }
        } else if (entitySelector.includesNonPlayers() && this.playersOnly && !entitySelector.isSenderOnly()) {
            reader.setCursor(0);
            throw PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.createWithContext(reader);
        } else {
            return entitySelector;
        }
    }

    public <S> ClientEntitySelector parse(StringReader stringReader, S object) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource commandSource) {
            StringReader stringReader = new StringReader(builder.getInput());
            stringReader.setCursor(builder.getStart());
            ClientEntitySelectorReader entitySelectorReader = new ClientEntitySelectorReader(stringReader);

            try {
                entitySelectorReader.read();
            } catch (CommandSyntaxException ignored) {
            }

            return entitySelectorReader.listSuggestions(builder, builderx -> {
                Collection<String> collection = commandSource.getPlayerNames();
                Iterable<String> iterable = this.playersOnly ? collection : Iterables.concat(collection, commandSource.getEntitySuggestions());
                CommandSource.suggestMatching(iterable, builderx);
            });
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Serializer implements ArgumentSerializer<ClientEntityArgumentType, Serializer.Properties> {
        private static final byte SINGLE_FLAG = 1;
        private static final byte PLAYERS_ONLY_FLAG = 2;

        @Override
        public void writePacket(Properties properties, PacketByteBuf packetByteBuf) {
            int i = 0;
            if (properties.single) {
                i |= 1;
            }

            if (properties.playersOnly) {
                i |= 2;
            }

            packetByteBuf.writeByte(i);
        }

        @Override
        public Properties fromPacket(PacketByteBuf packetByteBuf) {
            byte b = packetByteBuf.readByte();
            return new Serializer.Properties((b & 1) != 0, (b & 2) != 0);
        }

        public void writeJson(Properties properties, JsonObject jsonObject) {
            jsonObject.addProperty("amount", properties.single ? "single" : "multiple");
            jsonObject.addProperty("type", properties.playersOnly ? "players" : "entities");
        }

        public Properties getArgumentTypeProperties(ClientEntityArgumentType entityArgumentType) {
            return new Properties(entityArgumentType.singleTarget, entityArgumentType.playersOnly);
        }

        public final class Properties implements ArgumentSerializer.ArgumentTypeProperties<ClientEntityArgumentType> {
            final boolean single;
            final boolean playersOnly;

            Properties(final boolean single, final boolean playersOnly) {
                this.single = single;
                this.playersOnly = playersOnly;
            }

            public ClientEntityArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return new ClientEntityArgumentType(this.single, this.playersOnly);
            }

            @Override
            public ArgumentSerializer<ClientEntityArgumentType, ?> getSerializer() {
                return Serializer.this;
            }
        }
    }
}
