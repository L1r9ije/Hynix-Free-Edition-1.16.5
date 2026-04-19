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

/**
 * @param state The state of the event
 * @param type  The type of chunk event that occurred
 * @param x     The Chunk X position.
 * @param z     The Chunk Z position.
 * @author Brady
 * @since 8/2/2018
 */
public record ChunkEvent(EventState state, Type type, int x, int z) {

    /**
     * @return The state of the event
     */
    @Override
    public EventState state() {
        return this.state;
    }

    /**
     * @return The type of chunk event that occurred;
     */
    @Override
    public Type type() {
        return this.type;
    }

    /**
     * @return The Chunk X position.
     */
    @Override
    public int x() {
        return this.x;
    }

    /**
     * @return The Chunk Z position.
     */
    @Override
    public int z() {
        return this.z;
    }

    public enum Type {

        /**
         * When the chunk is constructed.
         */
        LOAD,

        /**
         * When the chunk is deconstructed.
         */
        UNLOAD,

        /**
         * When the chunk is being populated with blocks, tile entities, etc.
         * <p>
         * And it's a full chunk
         */
        POPULATE_FULL,

        /**
         * When the chunk is being populated with blocks, tile entities, etc.
         * <p>
         * And it's a partial chunk
         */
        POPULATE_PARTIAL
    }
}
