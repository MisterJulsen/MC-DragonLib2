/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.network.v2;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

/**
 * Reads the networking limits from the config.
 *
 * <p>Packets are often registered while the config is still loading, so every value falls back to
 * its default instead of failing.
 */
public final class NetworkSettings {

    private static final int KIB = 1024;

    private static final int DEFAULT_MAX_MESSAGE_BYTES = 16 * 1024 * KIB;
    private static final int DEFAULT_REASSEMBLY_BUDGET_BYTES = 64 * 1024 * KIB;
    private static final long DEFAULT_REASSEMBLY_TIMEOUT_MILLIS = 30_000L;
    private static final long DEFAULT_RESPONSE_TIMEOUT_MILLIS = 60_000L;
    private static final int DEFAULT_STREAM_CHUNK_BYTES = 256 * KIB;
    private static final int DEFAULT_STREAM_WINDOW = 8;

    private NetworkSettings() {}

    /**
     * Returns the default size limit for a single request or response payload.
     *
     * @return the message size limit in bytes
     */
    public static int getMaxMessageBytes() {
        return readInt(ModCommonConfig.NETWORK_MAX_MESSAGE_SIZE::get, DEFAULT_MAX_MESSAGE_BYTES / KIB) * KIB;
    }

    /**
     * Returns how much memory all unfinished incoming messages of one connection may occupy.
     *
     * @return the reassembly budget in bytes
     */
    public static int getReassemblyBudgetBytes() {
        return readInt(ModCommonConfig.NETWORK_REASSEMBLY_BUDGET::get, DEFAULT_REASSEMBLY_BUDGET_BYTES / KIB) * KIB;
    }

    /**
     * Returns how long an unfinished incoming message is kept.
     *
     * @return the reassembly timeout in milliseconds
     */
    public static long getReassemblyTimeoutMillis() {
        return readInt(ModCommonConfig.NETWORK_REASSEMBLY_TIMEOUT::get, (int) (DEFAULT_REASSEMBLY_TIMEOUT_MILLIS / 1000L)) * 1000L;
    }

    /**
     * Returns how long a request waits for its answer.
     *
     * @return the response timeout in milliseconds
     */
    public static long getResponseTimeoutMillis() {
        return readInt(ModCommonConfig.NETWORK_RESPONSE_TIMEOUT::get, (int) (DEFAULT_RESPONSE_TIMEOUT_MILLIS / 1000L)) * 1000L;
    }

    /**
     * Returns the default chunk size of a stream.
     *
     * @return the stream chunk size in bytes
     */
    public static int getStreamChunkBytes() {
        return readInt(ModCommonConfig.NETWORK_STREAM_CHUNK_SIZE::get, DEFAULT_STREAM_CHUNK_BYTES / KIB) * KIB;
    }

    /**
     * Returns how many stream chunks may be in flight before the sender waits.
     *
     * @return the stream window size in chunks
     */
    public static int getStreamWindow() {
        return readInt(ModCommonConfig.NETWORK_STREAM_WINDOW::get, DEFAULT_STREAM_WINDOW);
    }

    private static int readInt(Supplier<Integer> value, int fallback) {
        try {
            Integer result = value.get();
            return result == null ? fallback : result;
        } catch (Exception e) {
            return fallback;
        }
    }
}
