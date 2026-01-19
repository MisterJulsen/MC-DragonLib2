package de.mrjulsen.mcdragonlib.client.model.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.function.Function;

public record DLFaceData(int color, boolean ambientOcclusion, boolean emissive, List<String> tags) {
    public static final DLFaceData DEFAULT = new DLFaceData(0xFFFFFFFF, true, false, List.of());

    public static final Codec<Integer> COLOR = new EitherCodec<>(Codec.INT, Codec.STRING).xmap(
            either -> either.map(Function.identity(), str -> (int) Long.parseLong(str, 16)),
            color -> Either.right(Integer.toHexString(color)));
    public static final Codec<List<String>> STRING_LIST = Codec.STRING.listOf();


    public static final Codec<DLFaceData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            COLOR.optionalFieldOf("color", 0xFFFFFFFF).forGetter(DLFaceData::color),
            Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(DLFaceData::ambientOcclusion),
            Codec.BOOL.optionalFieldOf("emissive", false).forGetter(DLFaceData::emissive),
            STRING_LIST.optionalFieldOf("tags", List.of()).forGetter(DLFaceData::tags)
    ).apply(builder, DLFaceData::new));

    public static DLFaceData read(JsonElement obj, DLFaceData fallback) throws JsonParseException {
        return obj == null ? fallback : CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(false, JsonParseException::new);
    }
}
