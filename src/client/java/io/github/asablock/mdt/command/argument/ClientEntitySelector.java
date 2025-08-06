package io.github.asablock.mdt.command.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.asablock.mdt.ClientLoadedPlayerManager;
import io.github.asablock.mdt.mixin.WorldInvoker;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ClientEntitySelector {
    public static final BiConsumer<Vec3d, List<? extends Entity>> ARBITRARY = (pos, entities) -> {
    };
    private static final TypeFilter<Entity, ?> PASSTHROUGH_FILTER = new TypeFilter<>() {
        public Entity downcast(Entity entity) {
            return entity;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int limit;
    private final boolean includesNonPlayers;
    private final List<Predicate<Entity>> predicates;
    private final NumberRange.DoubleRange distance;
    private final Function<Vec3d, Vec3d> positionOffset;
    @Nullable
    private final Box box;
    private final BiConsumer<Vec3d, List<? extends Entity>> sorter;
    private final boolean senderOnly;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID uuid;
    private final TypeFilter<Entity, ?> entityFilter;
    private final boolean usesAt;

    public ClientEntitySelector(
            int count,
            boolean includesNonPlayers,
            List<Predicate<Entity>> predicates,
            NumberRange.DoubleRange distance,
            Function<Vec3d, Vec3d> positionOffset,
            @Nullable Box box,
            BiConsumer<Vec3d, List<? extends Entity>> sorter,
            boolean senderOnly,
            @Nullable String playerName,
            @Nullable UUID uuid,
            @Nullable EntityType<?> type,
            boolean usesAt
    ) {
        this.limit = count;
        this.includesNonPlayers = includesNonPlayers;
        this.predicates = predicates;
        this.distance = distance;
        this.positionOffset = positionOffset;
        this.box = box;
        this.sorter = sorter;
        this.senderOnly = senderOnly;
        this.playerName = playerName;
        this.uuid = uuid;
        this.entityFilter = type == null ? PASSTHROUGH_FILTER : type;
        this.usesAt = usesAt;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean includesNonPlayers() {
        return this.includesNonPlayers;
    }

    public boolean isSenderOnly() {
        return this.senderOnly;
    }

    public boolean isLocalWorldOnly() {
        return true;
    }

    public boolean usesAt() {
        return this.usesAt;
    }

    public Entity getEntity(FabricClientCommandSource source) throws CommandSyntaxException {
        List<? extends Entity> list = this.getEntities(source);
        if (list.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        } else if (list.size() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
        } else {
            return list.getFirst();
        }
    }

    public List<? extends Entity> getEntities(FabricClientCommandSource source) {
        if (!this.includesNonPlayers) {
            return this.getPlayers(source);
        } else if (this.playerName != null) {
            AbstractClientPlayerEntity clientPlayerEntity = ((ClientLoadedPlayerManager) source.getClient().getNetworkHandler()).mdt_getPlayer(this.playerName);
            return clientPlayerEntity == null ? List.of() : List.of(clientPlayerEntity);
        } else if (this.uuid != null) {
            for (Entity entity : source.getWorld().getEntities()) {
                if (this.uuid.equals(entity.getUuid()) && entity.getType().isEnabled(source.getEnabledFeatures())) {
                    return List.of(entity);
                }
            }
            return List.of();
        } else {
            Vec3d vec3d = this.positionOffset.apply(source.getPosition());
            Box box = this.getOffsetBox(vec3d);
            if (this.senderOnly) {
                Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
                return source.getEntity() != null && predicate.test(source.getEntity()) ? List.of(source.getEntity()) : List.of();
            } else {
                Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, source.getEnabledFeatures());
                List<Entity> list = new ObjectArrayList<>();
                this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);

                return this.getEntities(vec3d, list);
            }
        }
    }


    private void appendEntitiesFromWorld(List<Entity> entities, ClientWorld world, @Nullable Box box, Predicate<Entity> predicate) {
        int i = this.getAppendLimit();
        if (entities.size() < i) {
            if (box != null) {
                world.collectEntitiesByType(this.entityFilter, box, predicate, entities, i);
            } else {
                ((WorldInvoker) world).invokeGetEntityLookup().forEach(this.entityFilter, entity -> {
                    if (predicate.test(entity)) {
                        entities.add(entity);
                        if (entities.size() >= i) {
                            return LazyIterationConsumer.NextIteration.ABORT;
                        }
                    }
                    return LazyIterationConsumer.NextIteration.CONTINUE;
                });
            }
        }
    }

    private int getAppendLimit() {
        return this.sorter == ARBITRARY ? this.limit : Integer.MAX_VALUE;
    }

    public AbstractClientPlayerEntity getPlayer(FabricClientCommandSource source) throws CommandSyntaxException {
        List<AbstractClientPlayerEntity> list = this.getPlayers(source);
        if (list.size() != 1) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        } else {
            return list.getFirst();
        }
    }

    public List<AbstractClientPlayerEntity> getPlayers(FabricClientCommandSource source) {
        ClientLoadedPlayerManager manager = (ClientLoadedPlayerManager) source.getClient().getNetworkHandler();
        if (this.playerName != null) {
            AbstractClientPlayerEntity clientPlayerEntity = manager.mdt_getPlayer(this.playerName);
            return clientPlayerEntity == null ? List.of() : List.of(clientPlayerEntity);
        } else if (this.uuid != null) {
            AbstractClientPlayerEntity clientPlayerEntity = manager.mdt_getPlayer(this.uuid);
            return clientPlayerEntity == null ? List.of() : List.of(clientPlayerEntity);
        } else {
            Vec3d vec3d = this.positionOffset.apply(source.getPosition());
            Box box = this.getOffsetBox(vec3d);
            Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
            if (this.senderOnly) {
                ClientPlayerEntity clientPlayerEntity2 = source.getPlayer();
                if (predicate.test(clientPlayerEntity2)) {
                    return List.of(clientPlayerEntity2);
                }

                return List.of();
            } else {
                int i = this.getAppendLimit();
                List<AbstractClientPlayerEntity> list = new ArrayList<>();
                for (AbstractClientPlayerEntity abstractClientPlayerEntity : source.getWorld().getPlayers()) {
                    if (predicate.test(abstractClientPlayerEntity)) {
                        list.add(abstractClientPlayerEntity);
                        if (list.size() >= i) {
                            break;
                        }
                    }
                }
                return this.getEntities(vec3d, list);
            }
        }
    }

    @Nullable
    private Box getOffsetBox(Vec3d offset) {
        return this.box != null ? this.box.offset(offset) : null;
    }

    private Predicate<Entity> getPositionPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures) {
        boolean bl = enabledFeatures != null;
        boolean bl2 = box != null;
        boolean bl3 = !this.distance.isDummy();
        int i = (bl ? 1 : 0) + (bl2 ? 1 : 0) + (bl3 ? 1 : 0);
        List<Predicate<Entity>> list;
        if (i == 0) {
            list = this.predicates;
        } else {
            List<Predicate<Entity>> list2 = new ObjectArrayList<>(this.predicates.size() + i);
            list2.addAll(this.predicates);
            if (bl) {
                list2.add(entity -> entity.getType().isEnabled(enabledFeatures));
            }

            if (bl2) {
                list2.add(entity -> box.intersects(entity.getBoundingBox()));
            }

            if (bl3) {
                list2.add(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
            }

            list = list2;
        }

        return Util.allOf(list);
    }

    private <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities) {
        if (entities.size() > 1) {
            this.sorter.accept(pos, entities);
        }

        return entities.subList(0, Math.min(this.limit, entities.size()));
    }

    public static Text getNames(List<? extends Entity> entities) {
        return Texts.join(entities, Entity::getDisplayName);
    }
}
