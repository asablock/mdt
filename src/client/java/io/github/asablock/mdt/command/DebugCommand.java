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
