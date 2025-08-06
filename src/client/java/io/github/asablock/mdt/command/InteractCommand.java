package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.github.asablock.mdt.command.argument.ClientBlockPosArgumentType;
import io.github.asablock.mdt.CommandUtil;
import io.github.asablock.mdt.command.argument.ClientEntityArgumentType;
import io.github.asablock.mdt.command.argument.PEnumArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class InteractCommand {
    public static final ArgumentType<HandSI> HAND_ARGUMENT_TYPE = new PEnumArgumentType<>(HandSI.class);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("minteract")
                .then(literal("block").then(CommandUtil.chainedCommandBuilder(argument("pos", ClientBlockPosArgumentType.blockPos())).append("side", Direction.UP, new PEnumArgumentType<>(Direction.class)).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).append("insideblock", false, BoolArgumentType.bool()).append("againstworldborder", false, BoolArgumentType.bool()).executes(InteractCommand::executeBlock)))
                .then(literal("entity").then(CommandUtil.chainedCommandBuilder(argument("target", ClientEntityArgumentType.entity())).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).executes(InteractCommand::executeEntity)))
                .then(CommandUtil.chainedCommandBuilder(literal("item")).append("hand", HandSI.MAIN_HAND, HAND_ARGUMENT_TYPE).executes(InteractCommand::executeItem))
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

    public static int executeEntity(CommandContext<FabricClientCommandSource> context, Object[] args) throws CommandSyntaxException {
        Entity target = ClientEntityArgumentType.getEntity(context, "target");
        Hand hand = ((HandSI) args[0]).hand;

        MinecraftClient client = context.getSource().getClient();
        ClientPlayerInteractionManager cpim = client.interactionManager;
        if (cpim == null) return 0;
        ActionResult actionResult = cpim.interactEntity(client.player, target, hand);
        context.getSource().sendFeedback(Text.literal(String.valueOf(actionResult)));
        return actionResult != null && actionResult.isAccepted() ? 1 : 0;
    }

    public static int executeItem(CommandContext<FabricClientCommandSource> context, Object[] args) {
        Hand hand = ((HandSI) args[0]).hand;

        MinecraftClient client = context.getSource().getClient();
        ClientPlayerInteractionManager cpim = client.interactionManager;
        if (cpim == null) return 0;
        ActionResult actionResult = cpim.interactItem(client.player, hand);
        context.getSource().sendFeedback(Text.literal(String.valueOf(actionResult)));
        return actionResult != null && actionResult.isAccepted() ? 1 : 0;
    }

    public static int executeCrosshairTarget(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = context.getSource().getClient();
        if (client.crosshairTarget != null) {
            for (Hand hand : Hand.values()) {
                ItemStack itemStack = client.player.getStackInHand(hand);
                if (client.crosshairTarget != null) {
                    switch (client.crosshairTarget.getType()) {
                        case ENTITY:
                            EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
                            Entity entity = entityHitResult.getEntity();
                            ActionResult actionResult = client.interactionManager.interactEntityAtLocation(client.player, entity, entityHitResult, hand);
                            if (!actionResult.isAccepted()) {
                                actionResult = client.interactionManager.interactEntity(client.player, entity, hand);
                            }

                            if (actionResult instanceof ActionResult.Success success) {
                                if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                                    client.player.swingHand(hand);
                                }
                                context.getSource().sendFeedback(Text.literal("Used!"));
                                return 1;
                            }
                            break;
                        case BLOCK:
                            BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                            int i = itemStack.getCount();
                            ActionResult actionResult2 = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
                            if (actionResult2 instanceof ActionResult.Success success2) {
                                if (success2.swingSource() == ActionResult.SwingSource.CLIENT) {
                                    client.player.swingHand(hand);
                                    if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.interactionManager.hasCreativeInventory())) {
                                        client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                    }
                                }
                                context.getSource().sendFeedback(Text.literal("Used!"));
                                return 1;
                            }

                            if (actionResult2 instanceof ActionResult.Fail) {
                                context.getSource().sendFeedback(Text.literal("Used!"));
                                return 1;
                            }
                    }
                }

                if (!itemStack.isEmpty() && client.interactionManager.interactItem(client.player, hand) instanceof ActionResult.Success success3) {
                    if (success3.swingSource() == ActionResult.SwingSource.CLIENT) {
                        client.player.swingHand(hand);
                    }

                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                    context.getSource().sendFeedback(Text.literal("Used!"));
                    return 1;
                }
            }
        }
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
