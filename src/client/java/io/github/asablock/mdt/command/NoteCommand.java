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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.command.argument.NoteSubjectArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class NoteCommand {
    public static final Path NOTE_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("mdt").resolve("notes");
    public static final Map<String, List<Text>> NOTES = new HashMap<>();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("mnote")
                .then(argument("subject", NoteSubjectArgumentType.subject())
                        .then(
                                literal("add").then(argument("content", TextArgumentType.text(registryAccess)).executes(NoteCommand::executeAdd))
                        )
                        .then(
                                literal("read").executes(NoteCommand::executeReadAll)
                                        .then(argument("index", IntegerArgumentType.integer(1)).executes(NoteCommand::executeRead))
                        )
                        .then(
                                literal("delete").then(argument("index", IntegerArgumentType.integer(1)).executes(NoteCommand::executeDelete))
                        )
                        .then(
                                literal("clearall").executes(NoteCommand::executeClearAll)
                        )
                )
        );
    }

    public static Text withNumber(Text text, int number) {
        return Text.literal(number + ". ").append(text);
    }

    public static int executeAdd(CommandContext<FabricClientCommandSource> context) {
        String subject = NoteSubjectArgumentType.getSubject(context, "subject");
        Text content = context.getArgument("content", Text.class);
        List<Text> list = NOTES.computeIfAbsent(subject, s -> new ArrayList<>());
        list.add(content);
        Text t2 = withNumber(content, list.size());
        context.getSource().sendFeedback(Text.translatable("command.mdt.note.add", t2));
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReadAll(CommandContext<FabricClientCommandSource> context) {
        String subject = NoteSubjectArgumentType.getSubject(context, "subject");
        List<Text> list = NOTES.get(subject);
        if (list != null) {
            context.getSource().sendFeedback(Text.translatable("command.mdt.note.read.all.success", list.size(), subject));
            for (int i = 0; i < list.size(); i++) {
                Text text = list.get(i);
                context.getSource().sendFeedback(withNumber(text, i + 1));
            }
            return list.size();
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.note.no_subject", subject));
            return 0;
        }
    }

    public static int executeRead(CommandContext<FabricClientCommandSource> context) {
        String subject = NoteSubjectArgumentType.getSubject(context, "subject");
        List<Text> list = NOTES.get(subject);
        if (list != null) {
            int index = IntegerArgumentType.getInteger(context, "index");
            if (index <= list.size()) {
                Text note = list.get(index - 1);
                context.getSource().sendFeedback(withNumber(note, index));
                return Command.SINGLE_SUCCESS;
            } else {
                context.getSource().sendError(Text.translatable("command.mdt.note.no"));
                return 0;
            }
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.note.no_subject", subject));
            return 0;
        }
    }

    public static int executeDelete(CommandContext<FabricClientCommandSource> context) {
        String subject = NoteSubjectArgumentType.getSubject(context, "subject");
        List<Text> list = NOTES.get(subject);
        if (list != null) {
            int index = IntegerArgumentType.getInteger(context, "index");
            if (index <= list.size()) {
                Text note = list.remove(index - 1);
                context.getSource().sendFeedback(Text.translatable("command.mdt.note.delete", withNumber(note, index)));
                return Command.SINGLE_SUCCESS;
            } else {
                context.getSource().sendError(Text.translatable("command.mdt.note.no"));
                return 0;
            }
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.note.no_subject", subject));
            return 0;
        }
    }

    public static int executeClearAll(CommandContext<FabricClientCommandSource> context) {
        String subject = NoteSubjectArgumentType.getSubject(context, "subject");
        List<Text> list = NOTES.remove(subject);
        if (list != null) {
            int size = list.size();
            context.getSource().sendFeedback(Text.translatable("command.mdt.note.clear_all", size, subject));
            return size;
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.note.no_subject", subject));
            return 0;
        }
    }
}
