package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public record DataKey<T>(
	DataKeyStorage storage,
	String id,
	T defaultValue,
	DataType<T> type,
	CommandDataType<T> command,
	boolean save,
	boolean sync,
	boolean syncToAllClients,
	@Nullable BiConsumer<Player, T> onReceived,
	boolean allowClientUpdates,
	boolean skipLogging
) {
	public static final DataKeyStorage SERVER = new DataKeyStorage("server", true);
	public static final DataKeyStorage PLAYER = new DataKeyStorage("player", false);

	public static class Builder<T> {
		private final DataKeyStorage storage;
		private final String id;
		private final DataType<T> type;
		private final T defaultValue;
		private boolean save;
		private boolean sync;
		private boolean syncToAllClients;
		private BiConsumer<Player, T> onReceived;
		private boolean allowClientUpdates;
		private boolean skipLogging;

		Builder(DataKeyStorage storage, String id, DataType<T> type, T defaultValue) {
			this.storage = storage;
			this.id = id;
			this.type = type;
			this.defaultValue = defaultValue;
			this.save = false;
			this.sync = false;
			this.syncToAllClients = storage.alwaysSyncToAllClients;
			this.onReceived = null;
			this.allowClientUpdates = storage.alwaysSyncToAllClients;
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

		public DataKey<T> buildDummy() {
			return new DataKey<>(
				storage,
				id,
				defaultValue,
				type,
				CommandDataType.of(type),
				save,
				sync,
				syncToAllClients,
				onReceived,
				allowClientUpdates,
				skipLogging
			);
		}

		public DataKey<T> build() {
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

	@Nullable
	public T[] getEnumConstants() {
		return type.typeClass().isEnum() ? type.typeClass().getEnumConstants() : null;
	}
}
