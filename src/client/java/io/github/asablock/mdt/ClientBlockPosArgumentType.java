package io.github.asablock.mdt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.asablock.mdt.mixin.DefaultPosArgumentAccessor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ClientBlockPosArgumentType implements ArgumentType<DefaultPosArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");

    public static ClientBlockPosArgumentType blockPos() {
        return new ClientBlockPosArgumentType();
    }

    public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        DefaultPosArgument argument = context.getArgument(name, DefaultPosArgument.class);
        DefaultPosArgumentAccessor acc = (DefaultPosArgumentAccessor) argument;
        Vec3d vec3d = context.getSource().getPlayer().getPos();
        return BlockPos.ofFloored(new Vec3d(acc.getX().toAbsoluteCoordinate(vec3d.x), acc.getY().toAbsoluteCoordinate(vec3d.y), acc.getZ().toAbsoluteCoordinate(vec3d.z)));
    }

    public DefaultPosArgument parse(StringReader stringReader) throws CommandSyntaxException {
        return DefaultPosArgument.parse(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource)) {
            return Suggestions.empty();
        } else {
            String string = builder.getRemaining();
            Collection<CommandSource.RelativePosition> collection;
            if (!string.isEmpty() && string.charAt(0) == '^') {
                collection = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
            } else {
                collection = ((CommandSource)context.getSource()).getBlockPositionSuggestions();
            }

            return CommandSource.suggestPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

