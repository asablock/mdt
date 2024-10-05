package io.github.asablock.mdt.toggle;

import com.google.gson.JsonElement;

import java.util.Objects;

public abstract sealed class ToggleNode permits Toggle, ToggleDirectory {
    private final String simpleName;
    private final String name;
    private final ToggleDirectory parent;

    protected ToggleNode(ToggleDirectory parent, String simpleName) {
        Objects.requireNonNull(simpleName);
        this.simpleName = simpleName;
        this.parent = parent;
        if (this.parent != null) this.parent.add(this);

        StringBuilder sb = new StringBuilder(this.simpleName);
        if (parent != null) ((ToggleNode) parent).insert0(sb);
        this.name = sb.toString();
    }

    public abstract JsonElement encodeJson();

    public abstract void decodeJson(JsonElement source);

    public final String getSimpleName() {
        return simpleName;
    }

    protected abstract void insert(StringBuilder sb);

    private void insert0(StringBuilder sb) {
        insert(sb);
        if (parent != null) ((ToggleNode) parent).insert0(sb);
    }

    public final String getName() {
        return name;
    }

    public final ToggleDirectory getParent() {
        return parent;
    }

    public abstract int reset();

    public ToggleDirectory getAsDirectory() {
        throw new ClassCastException("Cannot cast to ToggleDirectory");
    }

    public Toggle<?> getAsToggle() {
        throw new ClassCastException("Cannot cast to Toggle<?>");
    }

    @Override
    public String toString() {
        return "ToggleNode[name=" + name + ",parent=" + parent.getName() + ']';
    }
}
