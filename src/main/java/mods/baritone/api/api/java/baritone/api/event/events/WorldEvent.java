/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package mods.baritone.api.api.java.baritone.api.event.events;

import mods.baritone.api.api.java.baritone.api.event.events.type.EventState;
import net.minecraft.client.world.ClientWorld;

/**
 * @param world The new world that is being loaded. {@code null} if being unloaded.
 * @param state The state of the event
 * @author Brady
 * @since 8/4/2018
 */
public record WorldEvent(ClientWorld world, EventState state) {

    /**
     * @return The new world that is being loaded. {@code null} if being unloaded.
     */
    @Override
    public ClientWorld world() {
        return this.world;
    }

    /**
     * @return The state of the event
     */
    @Override
    public EventState state() {
        return this.state;
    }
}
