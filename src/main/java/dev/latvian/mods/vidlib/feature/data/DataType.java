package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public record DataType<T>(
	DataTypeStorage storage,
	String id,
	T defaultValue,
	@Nullable KnownCodec<T> type,
	boolean save,
	boolean sync,
	boolean syncToAllClients,
	@Nullable BiConsumer<Player, T> onReceived,
	boolean allowClientUpdates,
	boolean skipLogging
) {
	public static final DataTypeStorage SERVER = new DataTypeStorage("server", true);
	public static final DataTypeStorage PLAYER = new DataTypeStorage("player", false);

	public static class Builder<T> {
		private final DataTypeStorage storage;
		private final String id;
		private final KnownCodec<T> type;
		private final T defaultValue;
		private boolean save;
		private boolean sync;
		private boolean syncToAllClients;
		private BiConsumer<Player, T> onReceived;
		private boolean allowClientUpdates;
		private boolean skipLogging;

		Builder(DataTypeStorage storage, String id, @Nullable KnownCodec<T> type, T defaultValue) {
			this.storage = storage;
			this.id = id;
			this.type = type;
			this.defaultValue = defaultValue;
			this.save = false;
			this.sync = false;
			this.syncToAllClients = storage.alwaysSyncToAllClients;
			this.onReceived = null;
			this.allowClientUpdates = false;
			this.skipLogging = false;
		}

		public Builder<T> save() {
			this.save = true;
			return this;
		}

		public Builder<T> sync() {
			this.sync = true;
			return this;
		}

		public Builder<T> syncToAllClients() {
			this.syncToAllClients = true;
			return this;
		}

		public Builder<T> onReceived(BiConsumer<Player, T> onReceived) {
			this.onReceived = onReceived;
			return this;
		}

		public Builder<T> allowClientUpdates() {
			this.allowClientUpdates = true;
			return this;
		}

		public Builder<T> skipLogging() {
			this.skipLogging = true;
			return this;
		}

		public DataType<T> buildDummy() {
			return new DataType<>(
				storage,
				id,
				defaultValue,
				type,
				save,
				sync,
				syncToAllClients,
				onReceived,
				allowClientUpdates,
				skipLogging
			);
		}

		public DataType<T> build() {
			var dataType = buildDummy();

			if (type != null) {
				storage.all.put(id, dataType);

				if (save) {
					storage.saved.put(id, dataType);
				}

				if (sync) {
					storage.synced.put(id, dataType);
				}
			}

			return dataType;
		}
	}

	@Override
	public String toString() {
		return "DataType[storage=" + storage + ", id=" + id + "]";
	}
}
