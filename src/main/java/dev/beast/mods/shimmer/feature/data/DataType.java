package dev.beast.mods.shimmer.feature.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record DataType<T>(
	DataTypeStorage storage,
	ResourceLocation id,
	T defaultValue,
	boolean identity,
	@Nullable Codec<T> codec,
	@Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
	boolean syncToAllClients,
	@Nullable Consumer<Player> onReceived
) {
	public static final DataTypeStorage SERVER = new DataTypeStorage(true);
	public static final DataTypeStorage PLAYER = new DataTypeStorage(false);

	public static class Builder<T> {
		private final DataTypeStorage storage;
		private final ResourceLocation id;
		private final T defaultValue;
		private boolean identity;
		private Codec<T> codec;
		private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
		private boolean syncToAllClients;
		private Consumer<Player> onReceived;

		Builder(DataTypeStorage storage, ResourceLocation id, T defaultValue) {
			this.storage = storage;
			this.id = id;
			this.defaultValue = defaultValue;
			this.identity = false;
			this.codec = null;
			this.streamCodec = null;
			this.syncToAllClients = storage.alwaysSyncToAllClients;
			this.onReceived = null;
		}

		public Builder<T> identity() {
			this.identity = true;
			return this;
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

		public Builder<T> onReceived(Consumer<Player> onReceived) {
			this.onReceived = onReceived;
			return this;
		}

		public DataType<T> build() {
			var type = new DataType<>(storage, id, defaultValue, identity, codec, streamCodec, syncToAllClients, onReceived);

			if (codec != null) {
				storage.saved.put(id, type);
			}

			if (streamCodec != null) {
				storage.synced.put(id, type);
			}

			return type;
		}
	}
}
