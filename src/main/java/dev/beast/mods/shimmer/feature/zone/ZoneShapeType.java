package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record ZoneShapeType<T extends ZoneShape>(String id, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	private static final Map<String, ZoneShapeType<?>> MAP = new HashMap<>();

	public static void register(ZoneShapeType<?> type) {
		MAP.put(type.id(), type);
	}

	public static final Codec<ZoneShapeType<?>> CODEC = Codec.STRING.xmap(MAP::get, ZoneShapeType::id);
	public static final StreamCodec<ByteBuf, ZoneShapeType<?>> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(MAP::get, ZoneShapeType::id);

	public ZoneShapeType(String id, MapCodec<T> codec) {
		this(id, codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
	}
}
