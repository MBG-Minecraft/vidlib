package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public abstract class SimpleRegistryType<V> {
	public static class Unit<V> extends SimpleRegistryType<V> {
		private final V instance;

		private Unit(ResourceLocation id, V instance) {
			super(id, MapCodec.unit(instance), StreamCodec.unit(instance));
			this.instance = instance;
		}

		public V instance() {
			return instance;
		}
	}

	public static class Dynamic<V> extends SimpleRegistryType<V> {
		private Dynamic(ResourceLocation id, MapCodec<? super V> codec, StreamCodec<? super RegistryFriendlyByteBuf, ? super V> streamCodec) {
			super(id, codec, streamCodec);
		}
	}

	public static <V> Unit<V> unit(ResourceLocation id, V instance) {
		return new Unit<>(id, instance);
	}

	public static <V> Dynamic<V> dynamic(ResourceLocation id, MapCodec<? super V> codec, StreamCodec<? super RegistryFriendlyByteBuf, ? super V> streamCodec) {
		return new Dynamic<>(id, codec, streamCodec);
	}

	private final ResourceLocation id;
	final MapCodec<? super V> codec;
	final StreamCodec<? super RegistryFriendlyByteBuf, ? super V> streamCodec;

	private SimpleRegistryType(ResourceLocation id, MapCodec<? super V> codec, StreamCodec<? super RegistryFriendlyByteBuf, ? super V> streamCodec) {
		this.id = id;
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	public ResourceLocation id() {
		return id;
	}

}
