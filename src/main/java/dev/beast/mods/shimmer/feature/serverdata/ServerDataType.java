package dev.beast.mods.shimmer.feature.serverdata;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record ServerDataType<T extends ServerData>(ResourceLocation id, Supplier<T> factory, @Nullable Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	static final Map<ResourceLocation, ServerDataType<?>> SAVED = new HashMap<>();
	private static final Map<ResourceLocation, ServerDataType<?>> SYNCED = new HashMap<>();

	static final Codec<ServerDataType<?>> CODEC = ShimmerCodecs.map(SAVED, ResourceLocation.CODEC, ServerDataType::id);
	static final StreamCodec<ByteBuf, ServerDataType<?>> STREAM_CODEC = ShimmerStreamCodecs.map(SYNCED, ResourceLocation.STREAM_CODEC, ServerDataType::id);

	public static class Builder<T extends ServerData> {
		private final ResourceLocation id;
		private final Supplier<T> factory;
		private Codec<T> codec;
		private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

		private Builder(ResourceLocation id, Supplier<T> factory) {
			this.id = id;
			this.factory = factory;
			this.codec = null;
			this.streamCodec = null;
		}

		public Builder<T> save(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public Builder<T> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
			this.streamCodec = streamCodec;
			return this;
		}

		public ServerDataType<T> build() {
			var type = new ServerDataType<>(id, factory, codec, streamCodec);

			if (codec != null) {
				SAVED.put(id, type);
			}

			if (streamCodec != null) {
				SYNCED.put(id, type);
			}

			return type;
		}
	}

	public static <T extends ServerData> Builder<T> builder(ResourceLocation id, Supplier<T> factory) {
		return new Builder<>(id, factory);
	}
}
