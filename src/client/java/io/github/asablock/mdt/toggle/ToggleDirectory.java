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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.asablock.mdt.Mdt;

import java.util.*;

public sealed class ToggleDirectory extends ToggleNode permits ToggleDirectory.Root {
    private final Set<ToggleNode> subNodes;
    private final Set<ToggleNode> unmodifiableView;

    public ToggleDirectory(ToggleDirectory parent, String name) {
        super(parent, name);
        this.subNodes = new HashSet<>();
        this.unmodifiableView = Collections.unmodifiableSet(subNodes);
    }

    public Set<ToggleNode> getSubNodes() {
        return unmodifiableView;
    }

    public boolean add(ToggleNode node) {
        return subNodes.add(node);
    }

    @Override
    public JsonElement encodeJson() {
        JsonObject root = new JsonObject();
        for (ToggleNode subNode : subNodes) {
            root.add(subNode.getSimpleName(), subNode.encodeJson());
        }
        return root;
    }

    @Override
    public void decodeJson(JsonElement source) {
        JsonObject root = source.getAsJsonObject();
        for (ToggleNode subNode : subNodes) {
            JsonElement e = root.get(subNode.getSimpleName());
            if (e != null) {
                subNode.decodeJson(e);
            } else {
                Mdt.LOGGER.warn("{} is not found in the source", subNode.getName());
            }
        }
    }

    @Override
    protected void insert(StringBuilder sb) {
        sb.insert(0, '.').insert(0, getSimpleName());
    }

    @Override
    public int reset() {
        int modifications = 0;
        for (ToggleNode subNode : subNodes) {
            modifications += subNode.reset();
        }
        return modifications;
    }

    @Override
    public ToggleDirectory getAsDirectory() {
        return this;
    }

    public static ToggleDirectory createRootNode() {
        return new Root();
    }

    public static final class Root extends ToggleDirectory {
        private Root() {
            super(null, "");
        }

        @Override
        protected void insert(StringBuilder sb) {
        }
    }

    @Override
    public String toString() {
        return "ToggleDirectory[name=" + getName() + ",parent=" + getParent().getName() + ']';
    }
}
