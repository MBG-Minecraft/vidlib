package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataKeyStorage {
	public final String name;
	public final boolean alwaysSyncToAllClients;
	public final Map<String, DataKey<?>> all;
	public final Map<String, DataKey<?>> saved;
	final Map<String, DataKey<?>> synced;
	public final StreamCodec<RegistryFriendlyByteBuf, DataMapValue> valueStreamCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, List<DataMapValue>> valueListStreamCodec;

	public DataKeyStorage(String name, boolean alwaysSyncToAllClients) {
		this.name = name;
		this.alwaysSyncToAllClients = alwaysSyncToAllClients;
		this.all = new LinkedHashMap<>();
		this.saved = new HashMap<>();
		this.synced = new HashMap<>();

		this.valueStreamCodec = new StreamCodec<>() {
			@Override
			public DataMapValue decode(RegistryFriendlyByteBuf buf) {
				var id = buf.readUtf();
				var key = synced.get(id);

				if (VidLibConfig.legacyDataKeyStream) {
					if (key == null) {
						throw new NullPointerException("Data type with id " + id + " not found. Available types in '" + DataKeyStorage.this.name + "': " + synced.keySet());
					}

					return new DataMapValue(key, key.type().streamCodec().decode(buf));
				}

				var decoded = VLStreamCodecs.decode(buf);

				if (key == null) {
					VidLib.LOGGER.error("Data type with id " + id + " not found. Available types in '" + DataKeyStorage.this.name + "': " + synced.keySet());
					return DataMapValue.INVALID;
				}

				var value = decoded.value();

				if (decoded.bytes() != null) {
					value = VLStreamCodecs.decodeValue(buf, key.type(), decoded.bytes());
				}

				return new DataMapValue(key, value);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, DataMapValue dataMapValue) {
				buf.writeUtf(dataMapValue.key().id());

				if (VidLibConfig.legacyDataKeyStream) {
					dataMapValue.key().type().streamCodec().encode(buf, Cast.to(dataMapValue.value()));
				} else {
					VLStreamCodecs.encode(buf, dataMapValue.key().type(), dataMapValue.value());
				}
			}
		};

		this.valueListStreamCodec = KLibStreamCodecs.listOf(valueStreamCodec);
	}

	public <T> DataKey.Builder<T> builder(String id, DataType<T> type, T defaultValue) {
		return new DataKey.Builder<>(this, id, type, defaultValue);
	}

	public <T> DataKey.Builder<T> buildDefault(String id, DataType<T> type, T defaultValue) {
		return builder(id, type, defaultValue).save().sync();
	}

	public <T> DataKey<T> createDefault(String id, DataType<T> type, T defaultValue) {
		return buildDefault(id, type, defaultValue).build();
	}

	public DataKey<Boolean> createDefaultBoolean(String id, boolean defaultValue) {
		return createDefault(id, DataTypes.BOOL, defaultValue);
	}

	public DataKey<Integer> createDefaultVarInt(String id, int defaultValue) {
		return createDefault(id, DataTypes.VAR_INT, defaultValue);
	}

	public DataKey<Integer> createDefaultTicks(String id, int defaultValue) {
		return createDefault(id, DataTypes.TICKS, defaultValue);
	}

	public DataKey<Float> createDefaultFloat(String id, float defaultValue) {
		return createDefault(id, DataTypes.FLOAT, defaultValue);
	}

	public DataKey<Double> createDefaultDouble(String id, double defaultValue) {
		return createDefault(id, DataTypes.DOUBLE, defaultValue);
	}

	@Override
	public String toString() {
		return name;
	}
}
