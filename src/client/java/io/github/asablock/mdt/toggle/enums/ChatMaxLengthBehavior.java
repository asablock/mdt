package io.github.asablock.mdt.toggle.enums;

import net.minecraft.util.StringIdentifiable;

public enum ChatMaxLengthBehavior implements StringIdentifiable {
    UNLIMITED("unlimited", false, false),
    VANILLA("vanilla", true, false),
    RESTRICTED_AFTER_SENDING("restrictedAfterSending", false, true);
    private final String string;
    private final boolean shallRestrictFieldMaxLength;
    private final boolean shallRestrictAfterSending;

    ChatMaxLengthBehavior(String string, boolean shallRestrictFieldMaxLength, boolean shallRestrictAfterSending) {
        this.string = string;
        this.shallRestrictFieldMaxLength = shallRestrictFieldMaxLength;
        this.shallRestrictAfterSending = shallRestrictAfterSending;
    }

    @Override
    public String asString() {
        return string;
    }

    public boolean shallRestrictFieldMaxLength() {
        return shallRestrictFieldMaxLength;
    }

    public boolean shallRestrictAfterSending() {
        return shallRestrictAfterSending;
    }
}
