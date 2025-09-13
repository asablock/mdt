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

package io.github.asablock.mdt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.github.asablock.mdt.command.argument.ClientBlockPosArgumentType;
import io.github.asablock.mdt.util.CommandUtil;
import io.github.asablock.mdt.command.argument.ClientEntityArgumentType;
import io.github.asablock.mdt.command.argument.PEnumArgumentType;
import io.github.asablock.mdt.util.Util;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class InteractCommand {
    public static final ArgumentType<HandSI> HAND_ARGUMENT_TYPE = new PEnumArgumentType<>(HandSI.class, HandSI.CODEC);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("minteract")
                .then(literal("block").then(CommandUtil.chainedCommandBuilder(argument("pos", ClientBlockPosArgumentType.blockPos())).append("side", Direction.UP, new PEnumArgumentType<>(Direction.class)).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).append("insideblock", false, BoolArgumentType.bool()).append("againstworldborder", false, BoolArgumentType.bool()).executes(InteractCommand::executeBlock)))
                .then(literal("entity").then(CommandUtil.chainedCommandBuilder(argument("target", ClientEntityArgumentType.entity())).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).executes(InteractCommand::executeEntity)))
                .then(CommandUtil.chainedCommandBuilder(literal("item")).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).executes(InteractCommand::executeItem))
                .then(literal("crosshairtarget").executes(InteractCommand::executeCrosshairTarget)));
    }

    private static void sendFeedback(CommandContext<FabricClientCommandSource> context, ActionResult actionResult) {
        context.getSource().sendFeedback(Text.translatable("command.mdt.interact.used_with_result", actionResult != null ? Util.actionResultToText(actionResult) : null));
    }

    public static int executeBlock(CommandContext<FabricClientCommandSource> context, Object[] args) {
        BlockPos pos = ClientBlockPosArgumentType.getBlockPos(context, "pos");
        Direction side = (Direction) args[0];
        Hand hand = ((HandSI) args[1]).hand;
        boolean insideBlock = (Boolean) args[2];
        boolean againstWorldBorder = (Boolean) args[3];

        BlockHitResult blockHitResult = new BlockHitResult(pos.toCenterPos(), side, pos, insideBlock, againstWorldBorder);

        MinecraftClient client = context.getSource().getClient();
        ClientPlayerInteractionManager cpim = client.interactionManager;
        if (cpim == null) return 0;
        ActionResult actionResult = cpim.interactBlock(client.player, hand, blockHitResult);
        sendFeedback(context, actionResult);
        return CommandUtil.success(actionResult != null && actionResult.isAccepted());
    }

    public static int executeEntity(CommandContext<FabricClientCommandSource> context, Object[] args) throws CommandSyntaxException {
        Entity target = ClientEntityArgumentType.getEntity(context, "target");
        Hand hand = ((HandSI) args[0]).hand;

        MinecraftClient client = context.getSource().getClient();
        ClientPlayerInteractionManager cpim = client.interactionManager;
        if (cpim == null) return 0;
        ActionResult actionResult = cpim.interactEntity(client.player, target, hand);
        sendFeedback(context, actionResult);
        return CommandUtil.success(actionResult != null && actionResult.isAccepted());
    }

    public static int executeItem(CommandContext<FabricClientCommandSource> context, Object[] args) {
        Hand hand = ((HandSI) args[0]).hand;

        MinecraftClient client = context.getSource().getClient();
        ClientPlayerInteractionManager cpim = client.interactionManager;
        if (cpim == null) return 0;
        ActionResult actionResult = cpim.interactItem(client.player, hand);
        sendFeedback(context, actionResult);
        return CommandUtil.success(actionResult != null && actionResult.isAccepted());
    }

    public static int executeCrosshairTarget(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = context.getSource().getClient();
        ActionResult actionResult = Util.applyHitResult(client.crosshairTarget);
        if (actionResult != null) {
            sendFeedback(context, actionResult);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendFeedback(Text.translatable("command.mdt.interact.both_pass"));
            return 0;
        }
    }

    public enum HandSI implements StringIdentifiable {
        MAIN_HAND("mainhand", Hand.MAIN_HAND),
        OFF_HAND("offhand", Hand.OFF_HAND);

        public static final Codec<HandSI> CODEC = StringIdentifiable.createCodec(HandSI::values);

        public final String string;
        public final Hand hand;

        HandSI(String string, Hand hand) {
            this.string = string;
            this.hand = hand;
        }

        @Override
        public String asString() {
            return string;
        }


        @Override
        public String toString() {
            return string;
        }
    }
}
