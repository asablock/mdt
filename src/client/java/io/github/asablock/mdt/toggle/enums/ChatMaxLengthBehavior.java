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
