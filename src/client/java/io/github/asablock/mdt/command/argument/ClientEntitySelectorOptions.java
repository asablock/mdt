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

package io.github.asablock.mdt.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.asablock.mdt.Mdt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.FloatRangeArgument;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.function.Predicate;

public class ClientEntitySelectorOptions {
    private static final Map<String, SelectorOption> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType(
            option -> Text.stringifiedTranslatable("argument.entity.options.unknown", option)
    );
    public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType(
            option -> Text.stringifiedTranslatable("argument.entity.options.inapplicable", option)
    );
    public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.entity.options.distance.negative")
    );
    public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.entity.options.limit.toosmall")
    );
    public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType(
            sortType -> Text.stringifiedTranslatable("argument.entity.options.sort.irreversible", sortType)
    );
    public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType(
            gameMode -> Text.stringifiedTranslatable("argument.entity.options.mode.invalid", gameMode)
    );
    public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(
            entity -> Text.stringifiedTranslatable("argument.entity.options.type.invalid", entity)
    );

    private static void putOption(String id, SelectorHandler handler, Predicate<ClientEntitySelectorReader> condition, Text description) {
        OPTIONS.put(id, new SelectorOption(handler, condition, description));
    }

    public static void register() {
        if (OPTIONS.isEmpty()) {
            putOption("name", reader -> {
                int i = reader.getReader().getCursor();
                boolean bl = reader.readNegationCharacter();
                String string = reader.getReader().readString();
                if (reader.excludesName() && !bl) {
                    reader.getReader().setCursor(i);
                    throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "name");
                } else {
                    if (bl) {
                        reader.setExcludesName(true);
                    } else {
                        reader.setSelectsName(true);
                    }

                    reader.addPredicate(entity -> entity.getName().getString().equals(string) != bl);
                }
            }, reader -> !reader.selectsName(), Text.translatable("argument.entity.options.name.description"));
            putOption(
                    "distance",
                    reader -> {
                        int i = reader.getReader().getCursor();
                        NumberRange.DoubleRange doubleRange = NumberRange.DoubleRange.parse(reader.getReader());
                        if ((doubleRange.min().isEmpty() || !(doubleRange.min().get() < 0.0))
                                && (doubleRange.max().isEmpty() || !(doubleRange.max().get() < 0.0))) {
                            reader.setDistance(doubleRange);
                        } else {
                            reader.getReader().setCursor(i);
                            throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext(reader.getReader());
                        }
                    },
                    reader -> reader.getDistance().isDummy(),
                    Text.translatable("argument.entity.options.distance.description")
            );
            putOption("x", reader -> {
                reader.setX(reader.getReader().readDouble());
            }, reader -> reader.getX() == null, Text.translatable("argument.entity.options.x.description"));
            putOption("y", reader -> {
                reader.setY(reader.getReader().readDouble());
            }, reader -> reader.getY() == null, Text.translatable("argument.entity.options.y.description"));
            putOption("z", reader -> {
                reader.setZ(reader.getReader().readDouble());
            }, reader -> reader.getZ() == null, Text.translatable("argument.entity.options.z.description"));
            putOption("dx", reader -> {
                reader.setDx(reader.getReader().readDouble());
            }, reader -> reader.getDx() == null, Text.translatable("argument.entity.options.dx.description"));
            putOption("dy", reader -> {
                reader.setDy(reader.getReader().readDouble());
            }, reader -> reader.getDy() == null, Text.translatable("argument.entity.options.dy.description"));
            putOption("dz", reader -> {
                reader.setDz(reader.getReader().readDouble());
            }, reader -> reader.getDz() == null, Text.translatable("argument.entity.options.dz.description"));
            putOption(
                    "x_rotation",
                    reader -> reader.setPitchRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees)),
                    reader -> reader.getPitchRange() == FloatRangeArgument.ANY,
                    Text.translatable("argument.entity.options.x_rotation.description")
            );
            putOption(
                    "y_rotation",
                    reader -> reader.setYawRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees)),
                    reader -> reader.getYawRange() == FloatRangeArgument.ANY,
                    Text.translatable("argument.entity.options.y_rotation.description")
            );
            putOption("limit", reader -> {
                int i = reader.getReader().getCursor();
                int j = reader.getReader().readInt();
                if (j < 1) {
                    reader.getReader().setCursor(i);
                    throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext(reader.getReader());
                } else {
                    reader.setLimit(j);
                    reader.setHasLimit(true);
                }
            }, reader -> !reader.isSenderOnly() && !reader.hasLimit(), Text.translatable("argument.entity.options.limit.description"));
            putOption("sort", reader -> {
                int i = reader.getReader().getCursor();
                String string = reader.getReader().readUnquotedString();
                reader.setSuggestionProvider((builder, consumer) -> CommandSource.suggestMatching(Arrays.asList("nearest", "furthest", "random", "arbitrary"), builder));

                reader.setSorter(switch (string) {
                    case "nearest" -> ClientEntitySelectorReader.NEAREST;
                    case "furthest" -> ClientEntitySelectorReader.FURTHEST;
                    case "random" -> ClientEntitySelectorReader.RANDOM;
                    case "arbitrary" -> ClientEntitySelector.ARBITRARY;
                    default -> {
                        reader.getReader().setCursor(i);
                        throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext(reader.getReader(), string);
                    }
                });
                reader.setHasSorter(true);
            }, reader -> !reader.isSenderOnly() && !reader.hasSorter(), Text.translatable("argument.entity.options.sort.description"));
            putOption("gamemode", reader -> {
                reader.setSuggestionProvider((builder, consumer) -> {
                    String stringx = builder.getRemaining().toLowerCase(Locale.ROOT);
                    boolean blx = !reader.excludesGameMode();
                    boolean bl2 = true;
                    if (!stringx.isEmpty()) {
                        if (stringx.charAt(0) == '!') {
                            blx = false;
                            stringx = stringx.substring(1);
                        } else {
                            bl2 = false;
                        }
                    }

                    for (GameMode gameModex : GameMode.values()) {
                        if (gameModex.getName().toLowerCase(Locale.ROOT).startsWith(stringx)) {
                            if (bl2) {
                                builder.suggest("!" + gameModex.getName());
                            }

                            if (blx) {
                                builder.suggest(gameModex.getName());
                            }
                        }
                    }

                    return builder.buildFuture();
                });
                int i = reader.getReader().getCursor();
                boolean bl = reader.readNegationCharacter();
                if (reader.excludesGameMode() && !bl) {
                    reader.getReader().setCursor(i);
                    throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "gamemode");
                } else {
                    String string = reader.getReader().readUnquotedString();
                    GameMode gameMode = GameMode.byName(string, null);
                    if (gameMode == null) {
                        reader.getReader().setCursor(i);
                        throw INVALID_MODE_EXCEPTION.createWithContext(reader.getReader(), string);
                    } else {
                        reader.setIncludesNonPlayers(false);
                        reader.addPredicate(entity -> {
                            if (!(entity instanceof AbstractClientPlayerEntity e)) {
                                return false;
                            } else {
                                GameMode gameMode2 = Mdt.getGameMode(MinecraftClient.getInstance(), e);
                                return bl == (gameMode2 != gameMode);
                            }
                        });
                        if (bl) {
                            reader.setExcludesGameMode(true);
                        } else {
                            reader.setSelectsGameMode(true);
                        }
                    }
                }
            }, reader -> !reader.selectsGameMode(), Text.translatable("argument.entity.options.gamemode.description"));
            putOption("team", reader -> {
                boolean bl = reader.readNegationCharacter();
                String string = reader.getReader().readUnquotedString();
                reader.addPredicate(entity -> {
                    if (!(entity instanceof LivingEntity)) {
                        return false;
                    } else {
                        AbstractTeam abstractTeam = entity.getScoreboardTeam();
                        String string2 = abstractTeam == null ? "" : abstractTeam.getName();
                        return string2.equals(string) != bl;
                    }
                });
                if (bl) {
                    reader.setExcludesTeam(true);
                } else {
                    reader.setSelectsTeam(true);
                }
            }, reader -> !reader.selectsTeam(), Text.translatable("argument.entity.options.team.description"));
            putOption("type", reader -> {
                reader.setSuggestionProvider((builder, consumer) -> {
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder, String.valueOf('!'));
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(tag -> tag.getTag().id()), builder, "!#");
                    if (!reader.excludesEntityType()) {
                        CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder);
                        CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(tag -> tag.getTag().id()), builder, String.valueOf('#'));
                    }

                    return builder.buildFuture();
                });
                int i = reader.getReader().getCursor();
                boolean bl = reader.readNegationCharacter();
                if (reader.excludesEntityType() && !bl) {
                    reader.getReader().setCursor(i);
                    throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "type");
                } else {
                    if (bl) {
                        reader.setExcludesEntityType();
                    }

                    if (reader.readTagCharacter()) {
                        TagKey<EntityType<?>> tagKey = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.fromCommandInput(reader.getReader()));
                        reader.addPredicate(entity -> entity.getType().isIn(tagKey) != bl);
                    } else {
                        Identifier identifier = Identifier.fromCommandInput(reader.getReader());
                        EntityType<?> entityType = Registries.ENTITY_TYPE.getOptionalValue(identifier).orElseThrow(() -> {
                            reader.getReader().setCursor(i);
                            return INVALID_TYPE_EXCEPTION.createWithContext(reader.getReader(), identifier.toString());
                        });
                        if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                            reader.setIncludesNonPlayers(false);
                        }

                        reader.addPredicate(entity -> Objects.equals(entityType, entity.getType()) != bl);
                        if (!bl) {
                            reader.setEntityType(entityType);
                        }
                    }
                }
            }, reader -> !reader.selectsEntityType(), Text.translatable("argument.entity.options.type.description"));
            putOption("tag", reader -> {
                boolean bl = reader.readNegationCharacter();
                String string = reader.getReader().readUnquotedString();
                reader.addPredicate(entity -> "".equals(string) ? entity.getCommandTags().isEmpty() != bl : entity.getCommandTags().contains(string) != bl);
            }, reader -> true, Text.translatable("argument.entity.options.tag.description"));
            putOption("nbt", reader -> {
                boolean bl = reader.readNegationCharacter();
                NbtCompound nbtCompound = new StringNbtReader(reader.getReader()).parseCompound();
                reader.addPredicate(entity -> {
                    NbtCompound nbtCompound2 = entity.writeNbt(new NbtCompound());
                    if (entity instanceof AbstractClientPlayerEntity clientPlayerEntity) {
                        ItemStack itemStack = clientPlayerEntity.getInventory().getMainHandStack();
                        if (!itemStack.isEmpty()) {
                            nbtCompound2.put("SelectedItem", itemStack.toNbt(clientPlayerEntity.getRegistryManager()));
                        }
                    }

                    return NbtHelper.matches(nbtCompound, nbtCompound2, true) != bl;
                });
            }, reader -> true, Text.translatable("argument.entity.options.nbt.description"));
            putOption("scores", reader -> {
                StringReader stringReader = reader.getReader();
                Map<String, NumberRange.IntRange> map = Maps.newHashMap();
                stringReader.expect('{');
                stringReader.skipWhitespace();

                while (stringReader.canRead() && stringReader.peek() != '}') {
                    stringReader.skipWhitespace();
                    String string = stringReader.readUnquotedString();
                    stringReader.skipWhitespace();
                    stringReader.expect('=');
                    stringReader.skipWhitespace();
                    NumberRange.IntRange intRange = NumberRange.IntRange.parse(stringReader);
                    map.put(string, intRange);
                    stringReader.skipWhitespace();
                    if (stringReader.canRead() && stringReader.peek() == ',') {
                        stringReader.skip();
                    }
                }

                stringReader.expect('}');
                if (!map.isEmpty()) {
                    reader.addPredicate(entity -> {
                        Scoreboard scoreboard = entity.getServer().getScoreboard();

                        for (Map.Entry<String, NumberRange.IntRange> entry : map.entrySet()) {
                            ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(entry.getKey());
                            if (scoreboardObjective == null) {
                                return false;
                            }

                            ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(entity, scoreboardObjective);
                            if (readableScoreboardScore == null) {
                                return false;
                            }

                            if (!entry.getValue().test(readableScoreboardScore.getScore())) {
                                return false;
                            }
                        }

                        return true;
                    });
                }

                reader.setSelectsScores(true);
            }, reader -> !reader.selectsScores(), Text.translatable("argument.entity.options.scores.description"));
        }
    }

    public static SelectorHandler getHandler(ClientEntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
        SelectorOption selectorOption = OPTIONS.get(option);
        if (selectorOption != null) {
            if (selectorOption.condition.test(reader)) {
                return selectorOption.handler;
            } else {
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
            }
        } else {
            reader.getReader().setCursor(restoreCursor);
            throw UNKNOWN_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
        }
    }

    public static void suggestOptions(ClientEntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
        String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);

        for (Map.Entry<String, SelectorOption> entry : OPTIONS.entrySet()) {
            if (entry.getValue().condition.test(reader) && entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) {
                suggestionBuilder.suggest(entry.getKey() + "=", entry.getValue().description);
            }
        }
    }

    public interface SelectorHandler {
        void handle(ClientEntitySelectorReader reader) throws CommandSyntaxException;
    }

    record SelectorOption(SelectorHandler handler, Predicate<ClientEntitySelectorReader> condition, Text description) {
    }
}
