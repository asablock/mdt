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

package io.github.asablock.mdt.toggle;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.asablock.mdt.Mdt;

import java.io.Reader;
import java.io.Writer;

public class ToggleSerializer {
    public static void saveToggles(Writer writer) throws JsonIOException {
        JsonObject root = new JsonObject();

        root.add("toggles", Toggles.root.encodeJson());

        Mdt.PRETTY_PRINTING_GSON.toJson(root, writer);
    }

    public static void readToggles(Reader reader) throws JsonIOException, JsonSyntaxException {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        Toggles.root.decodeJson(root.get("toggles"));
    }
}
