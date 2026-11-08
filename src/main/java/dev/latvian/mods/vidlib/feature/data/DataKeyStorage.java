package dev.latvian.mods.vidlib.feature.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DataKeyStorage<K> {
	public final String name;
	public final Map<String, DataKey<K, ?>> all;
	public final Map<String, DataKey<K, ?>> saved;
	final Map<String, DataKey<K, ?>> synced;
	public final StreamCodec<RegistryFriendlyByteBuf, DataMapValue<K>> valueStreamCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, List<DataMapValue<K>>> valueListStreamCodec;
	public final Codec<DataKey<K, ?>> dataKeyCodec;
	public final StreamCodec<ByteBuf, DataKey<K, ?>> dataKeyStreamCodec;

	public DataKeyStorage(String name) {
		this.name = name;
		this.all = new Object2ObjectLinkedOpenHashMap<>();
		this.saved = new Object2ObjectOpenHashMap<>();
		this.synced = new Object2ObjectOpenHashMap<>();

		this.valueStreamCodec = new StreamCodec<>() {
			private final DataMapValue<K> invalidValue = new DataMapValue<>(null, null);

			@Override
			public DataMapValue<K> decode(RegistryFriendlyByteBuf buf) {
				var id = buf.readUtf();
				var key = synced.get(id);

				var decoded = VLStreamCodecs.decode(buf);

				if (key == null) {
					VidLib.LOGGER.error("Data type with id " + id + " not found. Available types in '" + DataKeyStorage.this.name + "': " + synced.keySet());
					return invalidValue;
				}

				var value = decoded.value();

				if (decoded.bytes() != null) {
					value = VLStreamCodecs.decodeValue(buf, key.type, decoded.bytes());
				}

				return new DataMapValue<>(key, value);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, DataMapValue<K> dataMapValue) {
				buf.writeUtf(dataMapValue.key().id);
				VLStreamCodecs.encode(buf, dataMapValue.key().type, dataMapValue.value());
			}
		};

		this.valueListStreamCodec = KLibStreamCodecs.listOf(valueStreamCodec);

		this.dataKeyCodec = Codec.STRING.comapFlatMap(key -> {
			var dataKey = all.get(key);

			if (dataKey != null) {
				return DataResult.success(dataKey);
			} else {
				return DataResult.error(() -> name + " data key '" + key + "' not found");
			}
		}, k -> k.id);

		this.dataKeyStreamCodec = ByteBufCodecs.STRING_UTF8.map(s -> {
			var dataKey = all.get(s);

			if (dataKey != null) {
				return dataKey;
			} else {
				throw new NullPointerException("Data key '" + s + "' not found");
			}
		}, k -> k.id);
	}

	public <T> DataKey.Builder<K, T> builder(String id, DataType<T> type, T defaultValue) {
		return new DataKey.Builder<>(this, id, type, defaultValue);
	}

	public <T> DataKey.Builder<K, T> buildDefault(String id, DataType<T> type, @Nullable T defaultValue) {
		return builder(id, type, defaultValue).save().sync();
	}

	public <T> DataKey<K, T> createDefault(String id, DataType<T> type, @Nullable T defaultValue) {
		return buildDefault(id, type, defaultValue).build();
	}

	public DataKey<K, Boolean> createBoolean(String id, boolean defaultValue) {
		return createDefault(id, DataTypes.BOOL, defaultValue);
	}

	public DataKey<K, Integer> createInt(String id, int defaultValue, int min, int max) {
		return createDefault(id, DataTypes.INT, defaultValue);
	}

	public DataKey<K, Integer> createVarInt(String id, int defaultValue, int min, int max) {
		return createDefault(id, DataTypes.VAR_INT, defaultValue);
	}

	public DataKey<K, Long> createLong(String id, long defaultValue, long min, long max) {
		return createDefault(id, DataTypes.LONG, defaultValue);
	}

	public DataKey<K, Long> createVarLong(String id, long defaultValue, long min, long max) {
		return createDefault(id, DataTypes.VAR_LONG, defaultValue);
	}

	public DataKey<K, Integer> createTicks(String id, int defaultValue, int min, int max) {
		return createDefault(id, DataTypes.TICKS, defaultValue);
	}

	public DataKey<K, Float> createFloat(String id, float defaultValue, float min, float max) {
		return createDefault(id, DataTypes.FLOAT, defaultValue);
	}

	public DataKey<K, Double> createDouble(String id, double defaultValue, double min, double max) {
		return createDefault(id, DataTypes.DOUBLE, defaultValue);
	}

	public <E> DataKey<K, E> createEnum(String id, DataType<E> type, E defaultValue) {
		return createDefault(id, type, defaultValue);
	}

	@Override
	public String toString() {
		return name;
	}
}
