package dev.beast.mods.shimmer.feature.session;

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

public record PlayerDataType<T extends PlayerData>(ResourceLocation id, Supplier<T> factory, @Nullable Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean syncToAllClients) {
	static final Map<ResourceLocation, PlayerDataType<?>> SAVED = new HashMap<>();
	private static final Map<ResourceLocation, PlayerDataType<?>> SYNCED = new HashMap<>();

	static final Codec<PlayerDataType<?>> CODEC = ShimmerCodecs.map(SAVED, ResourceLocation.CODEC, PlayerDataType::id);
	static final StreamCodec<ByteBuf, PlayerDataType<?>> STREAM_CODEC = ShimmerStreamCodecs.map(SYNCED, ResourceLocation.STREAM_CODEC, PlayerDataType::id);

	public static class Builder<T extends PlayerData> {
		private final ResourceLocation id;
		private final Supplier<T> factory;
		private Codec<T> codec;
		private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
		private boolean syncToAllClients;

		private Builder(ResourceLocation id, Supplier<T> factory) {
			this.id = id;
			this.factory = factory;
			this.codec = null;
			this.streamCodec = null;
			this.syncToAllClients = false;
		}

		public Builder<T> save(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public Builder<T> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
			this.streamCodec = streamCodec;
			this.syncToAllClients = false;
			return this;
		}

		public Builder<T> syncToAllClients() {
			this.syncToAllClients = true;
			return this;
		}

		public PlayerDataType<T> build() {
			var type = new PlayerDataType<>(id, factory, codec, streamCodec, syncToAllClients);

			if (codec != null) {
				SAVED.put(id, type);
			}

			if (streamCodec != null) {
				SYNCED.put(id, type);
			}

			return type;
		}
	}

	public static <T extends PlayerData> Builder<T> builder(ResourceLocation id, Supplier<T> factory) {
		return new Builder<>(id, factory);
	}
}
