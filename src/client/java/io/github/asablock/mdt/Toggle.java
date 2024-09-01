package io.github.asablock.mdt;

public class Toggle {
    public final String name;
    public final boolean defaultValue;
    public boolean enabled;

    public Toggle(String name, boolean defaultValue) {
        this.name = name;
        this.enabled = defaultValue;
        this.defaultValue = defaultValue;
    }

    public boolean reset() {
        boolean bl = enabled != defaultValue;
        enabled = defaultValue;
        return bl;
    }
}
