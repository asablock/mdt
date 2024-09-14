package io.github.asablock.mdt.toggle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
        return null;
    }

    @Override
    public void decodeJson(JsonElement source) {
        JsonObject root = source.getAsJsonObject();
        for (ToggleNode subNode : subNodes) {
            subNode.decodeJson(root.get(subNode.getSimpleName()));
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
}
