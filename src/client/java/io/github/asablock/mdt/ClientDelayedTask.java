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

package io.github.asablock.mdt;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public final class ClientDelayedTask {
    private static final List<ClientDelayedTask> TASKS = new ArrayList<>();

    private final long taskTimestamp;
    private final Consumer<? super MinecraftClient> action;
    private boolean scheduled;
    private boolean executed;

    public ClientDelayedTask(long taskTimestamp, Consumer<? super MinecraftClient> action) {
        this.taskTimestamp = taskTimestamp;
        this.action = action;
        this.scheduled = false;
        this.executed = false;
    }

    public void cancel() {
        TASKS.remove(this);
    }

    public long getTaskTimestamp() {
        return taskTimestamp;
    }

    public Consumer<? super MinecraftClient> getAction() {
        return action;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Override
    public String toString() {
        return "ClientDelayedTask[" +
               "action=" + action + ", " +
               "taskTimestamp=" + taskTimestamp + ']';
    }

    public static boolean schedule(ClientDelayedTask task) {
        if (!task.scheduled) {
            TASKS.add(task);
            task.scheduled = true;
            return true;
        } else {
            return false;
        }
    }

    static void registerEvent() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            long time = System.currentTimeMillis();
            Iterator<ClientDelayedTask> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                ClientDelayedTask task = iterator.next();
                if (task.taskTimestamp <= time) {
                    task.action.accept(client);
                    task.executed = true;
                    iterator.remove();
                }
            }
        });
    }
}
