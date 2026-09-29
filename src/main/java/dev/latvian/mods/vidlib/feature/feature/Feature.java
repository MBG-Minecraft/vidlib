package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class Feature {
	private static final Map<ResourceLocation, Feature> MAP = new HashMap<>();

	public static Feature create(ResourceLocation id) {
		return MAP.computeIfAbsent(id, Feature::new);
	}

	public static final StreamCodec<ByteBuf, Feature> STREAM_CODEC = ID.STREAM_CODEC.map(Feature::create, f -> f.id);

	public static final Feature INFINITE_CHUNK_RENDERING = create(ID.vidlib("infinite_chunk_rendering"));
	public static final Feature SERVER_DATA = create(ID.vidlib("server_data"));
	public static final Feature PLAYER_DATA = create(ID.vidlib("player_data"));

	public static final Feature SMALL_GRASS_HITBOX = create(ID.vidlib("small_grass_hitbox"));
	public static final Feature SOFT_BARRIERS = create(ID.vidlib("soft_barriers"));
	public static final Feature SKYBOX = create(ID.vidlib("skybox"));

	public final ResourceLocation id;

	private Feature(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
