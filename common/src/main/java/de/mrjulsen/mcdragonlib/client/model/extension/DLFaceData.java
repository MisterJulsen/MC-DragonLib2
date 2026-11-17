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

package de.mrjulsen.mcdragonlib.client.model.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DLFaceData(boolean ambientOcclusion, boolean emissive, List<String> tags) {
    public static final DLFaceData DEFAULT = new DLFaceData(true, false, List.of());

    public static final Codec<List<String>> STRING_LIST = Codec.STRING.listOf();


    public static final Codec<DLFaceData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(DLFaceData::ambientOcclusion),
            Codec.BOOL.optionalFieldOf("emissive", false).forGetter(DLFaceData::emissive),
            STRING_LIST.optionalFieldOf("tags", List.of()).forGetter(DLFaceData::tags)
    ).apply(builder, DLFaceData::new));

    public static DLFaceData read(JsonElement obj, DLFaceData fallback) throws JsonParseException {
        return obj == null ? fallback : CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(false, JsonParseException::new);
    }
}
