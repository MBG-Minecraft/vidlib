package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
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

	public static final Feature INFINITE_CHUNK_RENDERING = create(VidLib.id("infinite_chunk_rendering"));
	public static final Feature SERVER_DATA = create(VidLib.id("server_data"));
	public static final Feature PLAYER_DATA = create(VidLib.id("player_data"));

	public static final Feature SMALL_GRASS_HITBOX = create(VidLib.id("small_grass_hitbox"));
	public static final Feature SOFT_BARRIERS = create(VidLib.id("soft_barriers"));
	public static final Feature SKYBOX = create(VidLib.id("skybox"));

	public final ResourceLocation id;

	private Feature(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
