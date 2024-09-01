package io.github.asablock.mdt;

public class Toggle {
    public final String name;
    public boolean enabled;

    public Toggle(String name, boolean defaultValue) {
        this.name = name;
        this.enabled = defaultValue;
    }
}
