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
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DebugCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mdebug").then(literal("crosshairTarget").executes(DebugCommand::executeCrosshairTarget)));
    }

    public static int executeCrosshairTarget(CommandContext<FabricClientCommandSource> context) {
        HitResult hitResult = context.getSource().getClient().crosshairTarget;
        if (hitResult != null) {
            if (hitResult instanceof BlockHitResult bhr) {
                context.getSource().sendFeedback(Text.literal(String.format("BlockHitResult(pos=%s,blockPos=%s,side=%s,insideBlock=%s,againstWorldBorder=%s,missed=%s)", bhr.getPos(), bhr.getBlockPos(), bhr.getSide(), bhr.isInsideBlock(), bhr.isAgainstWorldBorder(), bhr.getType() == HitResult.Type.MISS)));
            } else if (hitResult instanceof EntityHitResult ehr) {
                context.getSource().sendFeedback(Text.literal(String.format("EntityHitResult(pos=%s,entity=%s)", ehr.getPos(), ehr.getEntity().getUuidAsString())));
            }
        } else {
            context.getSource().sendFeedback(Text.literal("null"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
