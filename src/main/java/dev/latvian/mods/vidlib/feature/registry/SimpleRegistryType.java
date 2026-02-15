package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public abstract class SimpleRegistryType<V extends SimpleRegistryEntry> {
	public static class Unit<V extends SimpleRegistryEntry> extends SimpleRegistryType<V> {
		private final V instance;

		private Unit(String id, Function<SimpleRegistryType<V>, V> factory) {
			super(id);
			this.instance = factory.apply(this);
			this.codec = MapCodec.unit(instance);
			this.streamCodec = StreamCodec.unit(instance);
		}

		public V instance() {
			return instance;
		}
	}

	public static class Dynamic<V extends SimpleRegistryEntry> extends SimpleRegistryType<V> {
		private Dynamic(String id, MapCodec<V> codec, StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec) {
			super(id);
			this.codec = codec;
			this.streamCodec = streamCodec;
		}
	}

	public static <V extends SimpleRegistryEntry> Unit<V> unitWithType(String id, Function<SimpleRegistryType<V>, V> instance) {
		return new Unit<>(id, instance);
	}

	public static <V extends SimpleRegistryEntry> Unit<V> unit(String id, V instance) {
		return new Unit<>(id, t -> instance);
	}

	public static <V extends SimpleRegistryEntry> Dynamic<V> dynamic(String id, MapCodec<V> codec, StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec) {
		return new Dynamic<>(id, codec, streamCodec);
	}

	private final String id;
	protected MapCodec<V> codec;
	protected StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec;
	int index;

	private SimpleRegistryType(String id) {
		this.id = id;
		this.index = -1;
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
