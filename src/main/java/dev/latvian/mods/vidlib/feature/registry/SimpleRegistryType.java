package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class SimpleRegistryType<V> {
	public static class Unit<V> extends SimpleRegistryType<V> {
		private final V instance;

		private Unit(String id, V instance) {
			super(id, MapCodec.unit(instance), StreamCodec.unit(instance));
			this.instance = instance;
		}

		public V instance() {
			return instance;
		}
	}

	public static class Dynamic<V> extends SimpleRegistryType<V> {
		private Dynamic(String id, MapCodec<V> codec, StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec) {
			super(id, codec, streamCodec);
		}
	}

	public static <V> Unit<V> unit(String id, V instance) {
		return new Unit<>(id, instance);
	}

	public static <V> Dynamic<V> dynamic(String id, MapCodec<V> codec, StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec) {
		return new Dynamic<>(id, codec, streamCodec);
	}

	private final String id;
	private final MapCodec<V> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec;

	private SimpleRegistryType(String id, MapCodec<V> codec, StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec) {
		this.id = id;
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	public MapCodec<V> codec() {
		return codec;
	}

	public StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec() {
		return streamCodec;
	}

	public String id() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}
}
