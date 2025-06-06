package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

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

				if (key == null) {
					throw new NullPointerException("Data type with id " + id + " not found. Available types in '" + DataKeyStorage.this.name + "': " + synced.keySet());
				}

				return new DataMapValue(key, key.type().type().streamCodec().decode(buf));
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, DataMapValue value) {
				buf.writeUtf(value.key().id());
				value.key().type().type().streamCodec().encode(buf, Cast.to(value.value()));
			}
		};

		this.valueListStreamCodec = valueStreamCodec.listOf();
	}

	public <T> DataKey.Builder<T> builder(String id, @Nullable RegisteredDataType<T> type, T defaultValue) {
		return new DataKey.Builder<>(this, id, type, defaultValue);
	}

	public <T> DataKey.Builder<T> buildDefault(String id, @Nullable RegisteredDataType<T> type, T defaultValue) {
		return builder(id, type, defaultValue).save().sync();
	}

	public <T> DataKey<T> createDefault(String id, @Nullable RegisteredDataType<T> type, T defaultValue) {
		return buildDefault(id, type, defaultValue).build();
	}

	public DataKey<Boolean> createDefaultBoolean(String id, boolean defaultValue) {
		return createDefault(id, RegisteredDataType.BOOL, defaultValue);
	}

	public DataKey<Integer> createDefaultVarInt(String id, int defaultValue) {
		return createDefault(id, RegisteredDataType.VAR_INT, defaultValue);
	}

	public DataKey<Float> createDefaultFloat(String id, float defaultValue) {
		return createDefault(id, RegisteredDataType.FLOAT, defaultValue);
	}

	public DataKey<Double> createDefaultDouble(String id, double defaultValue) {
		return createDefault(id, RegisteredDataType.DOUBLE, defaultValue);
	}

	@Override
	public String toString() {
		return name;
	}
}
