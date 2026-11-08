package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;

import java.util.ArrayList;
import java.util.List;

public class DataKey<K, T> {
	@FunctionalInterface
	public interface UpdateCallback<K, T> {
		void onUpdate(GlobalSessionData worldData, K key, T value);
	}

	public static final DataKeyStorage<GlobalSessionData> GLOBAL = new DataKeyStorage<>("global");
	public static final DataKeyStorage<LevelSessionData<?>> LEVEL = new DataKeyStorage<>("level");
	public static final DataKeyStorage<PlayerSessionData> PLAYER = new DataKeyStorage<>("player");

	public static class Builder<K, T> {
		private final DataKeyStorage<K> storage;
		private final String id;
		private final DataType<T> type;
		private final T defaultValue;
		private boolean save;
		private boolean sync;
		private boolean allowClientUpdates;
		private boolean skipLogging;

		Builder(DataKeyStorage<K> storage, String id, DataType<T> type, T defaultValue) {
			this.storage = storage;
			this.id = id;
			this.type = type;
			this.defaultValue = defaultValue;
			this.save = false;
			this.sync = false;
			this.allowClientUpdates = false;
			this.skipLogging = false;
		}

		public Builder<K, T> save() {
			this.save = true;
			return this;
		}

		public Builder<K, T> sync() {
			this.sync = true;
			return this;
		}

		public Builder<K, T> allowClientUpdates() {
			this.allowClientUpdates = true;
			return this;
		}

		public Builder<K, T> skipLogging() {
			this.skipLogging = true;
			return this;
		}

		public DataKey<K, T> build() {
			var dataType = new DataKey<>(
				storage,
				storage.all.size(),
				id,
				defaultValue,
				type,
				save,
				sync,
				new ArrayList<>(0),
				allowClientUpdates,
				skipLogging
			);

			storage.all.put(id, dataType);

			if (save) {
				storage.saved.put(id, dataType);
			}

			if (sync) {
				storage.synced.put(id, dataType);
			}

			return dataType;
		}
	}

	public final DataKeyStorage<K> storage;
	public final int index;
	public final String id;
	public final T defaultValue;
	public final DataType<T> type;
	public final boolean save;
	public final boolean sync;
	public final List<UpdateCallback<K, T>> onReceived;
	public final boolean allowClientUpdates;
	public final boolean skipLogging;

	private DataKey(
		DataKeyStorage<K> storage,
		int index,
		String id,
		T defaultValue,
		DataType<T> type,
		boolean save,
		boolean sync,
		List<UpdateCallback<K, T>> onReceived,
		boolean allowClientUpdates,
		boolean skipLogging
	) {
		this.storage = storage;
		this.index = index;
		this.id = id;
		this.defaultValue = defaultValue;
		this.type = type;
		this.save = save;
		this.sync = sync;
		this.onReceived = onReceived;
		this.allowClientUpdates = allowClientUpdates;
		this.skipLogging = skipLogging;
	}

	@Override
	public String toString() {
		return storage + "/" + id;
	}

	public void addUpdateListener(UpdateCallback<K, T> onReceived) {
		this.onReceived.add(onReceived);
	}
}
