package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import io.github.asablock.mdt.ClientBlockPosArgumentType;
import io.github.asablock.mdt.CommandUtil;
import io.github.asablock.mdt.PEnumArgumentType;
import io.github.asablock.mdt.mixin.MinecraftClientInvoker;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class InteractCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("minteract")
                //.then(literal("block").then(argument("pos", ClientBlockPosArgumentType.blockPos()).then(argument("side", new PEnumArgumentType<>(Direction.CODEC, Direction::values)).then(argument("hand", new PEnumArgumentType<>(HandSI.CODEC, HandSI::values)).then(argument("insideblock", BoolArgumentType.bool()).executes(InteractCommand::executeBlock))))))
                .then(literal("block").then(CommandUtil.chainedCommandBuilder(argument("pos", ClientBlockPosArgumentType.blockPos())).append("side", Direction.UP, new PEnumArgumentType<>(Direction.CODEC, Direction::values)).append("hand", HandSI.MAIN_HAND, new PEnumArgumentType<>(HandSI.CODEC, HandSI::values)).append("insideblock", false, BoolArgumentType.bool()).append("againstworldborder", false, BoolArgumentType.bool()).executes(InteractCommand::executeBlock)))
                .then(literal("crosshairtarget").executes(InteractCommand::executeCrosshairTarget)));
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
        context.getSource().sendFeedback(Text.literal(String.valueOf(actionResult)));
        return actionResult != null && actionResult.isAccepted() ? 1 : 0;
    }

    public static int executeCrosshairTarget(CommandContext<FabricClientCommandSource> context) {
        ((MinecraftClientInvoker) context.getSource().getClient()).invokeDoItemUse();
        context.getSource().sendFeedback(Text.literal("Used!"));
        return 1;
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
