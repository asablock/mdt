package io.github.asablock.mdt.toggle;

public interface TogglePreset {
    String getName();

    <T> T getValue(Toggle<T> toggle);
}
