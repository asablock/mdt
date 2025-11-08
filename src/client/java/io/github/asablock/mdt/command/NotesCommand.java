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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.asablock.mdt.Mdt;
import io.github.asablock.mdt.mixin.TextSerializationInvoker;
import io.github.asablock.mdt.util.IOUtil;
import io.github.asablock.mdt.util.Util;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class NotesCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mnotes")
                .then(literal("list").executes(NotesCommand::executeList))
                .then(literal("readall").executes(NotesCommand::executeReadAll))
                .then(literal("save").executes(NotesCommand::executeSave))
                .then(literal("reload").executes(NotesCommand::executeReload))
        );
    }

    public static int executeList(CommandContext<FabricClientCommandSource> context) {
        MutableText mutableText = Text.empty();
        boolean comma = false;
        for (final String subject : NoteCommand.NOTES.keySet()) {
            MutableText subjectText = Text.literal(subject)
                    .formatted(Formatting.BOLD)
                    .styled(style -> {
                        String command = "/mnote " + Util.quotedEscape(subject) + " read";
                        return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(command)));
                    });
            if (!comma) {
                comma = true;
            } else {
                subjectText = Text.literal(", ").append(subjectText);
            }
            mutableText.append(subjectText);
        }
        context.getSource().sendFeedback(mutableText);
        return NoteCommand.NOTES.size();
    }

    public static int executeReadAll(CommandContext<FabricClientCommandSource> context) {
        for (Map.Entry<String, List<Text>> entry : NoteCommand.NOTES.entrySet()) {
            context.getSource().sendFeedback(Text.literal(entry.getKey()));
            List<Text> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                Text text = list.get(i);
                context.getSource().sendFeedback(NoteCommand.withNumber(text, i + 1));
            }
        }
        return NoteCommand.NOTES.size();
    }

    public static int executeSave(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            save(context.getSource().getRegistryManager());
            context.getSource().sendFeedback(Text.translatable("command.mdt.notes.saved"));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace(new PrintWriter(IOUtil.getTextWriter(context.getSource()::sendError)));
            Mdt.LOGGER.error("Cannot save notes", e);
            throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
        }
    }

    public static int executeReload(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            reload();
            context.getSource().sendFeedback(Text.translatable("command.mdt.notes.reloaded"));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace(new PrintWriter(IOUtil.getTextWriter(context.getSource()::sendError)));
            Mdt.LOGGER.error("Cannot reload notes", e);
            throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
        }
    }

    public static void save(RegistryWrapper.WrapperLookup registries) throws IOException {
        Files.createDirectories(NoteCommand.NOTE_DIRECTORY);
        for (Map.Entry<String, List<Text>> entry : NoteCommand.NOTES.entrySet()) {
            Path path = NoteCommand.NOTE_DIRECTORY.resolve(entry.getKey() + ".json");
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            JsonArray array = new JsonArray();
            for (Text text : entry.getValue()) {
                array.add(TextSerializationInvoker.invokeToJson(text, registries));
            }
            root.add("notes", array);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                Mdt.PRETTY_PRINTING_GSON.toJson(root, bw);
            }
        }
    }

    public static void reload() throws IOException {
        NoteCommand.NOTES.clear();
        load(Util.getWorldDynamicRegistryManager());
    }

    public static void load(final RegistryWrapper.WrapperLookup registries) throws IOException {
        try (Stream<Path> stream = Files.list(NoteCommand.NOTE_DIRECTORY)) {
            stream.forEach(path -> {
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(".json")) {
                    String subject = fileName.substring(0, fileName.length() - 5);
                    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        JsonObject root = JsonParser.parseReader(br).getAsJsonObject();
                        JsonArray array = root.getAsJsonArray("notes");
                        List<Text> list = NoteCommand.NOTES.computeIfAbsent(subject, s -> new ArrayList<>());
                        for (JsonElement elem : array) {
                            list.add(Text.Serialization.fromJsonTree(elem, registries));
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }
}
