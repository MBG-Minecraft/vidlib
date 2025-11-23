package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.DoubleImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.IntImBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DataKeyStorage {
	public final String name;
	public final Map<String, DataKey<?>> all;
	public final Map<String, DataKey<?>> saved;
	final Map<String, DataKey<?>> synced;
	public final StreamCodec<RegistryFriendlyByteBuf, DataMapValue> valueStreamCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, List<DataMapValue>> valueListStreamCodec;

	public DataKeyStorage(String name) {
		this.name = name;
		this.all = new Object2ObjectLinkedOpenHashMap<>();
		this.saved = new Object2ObjectOpenHashMap<>();
		this.synced = new Object2ObjectOpenHashMap<>();

		this.valueStreamCodec = new StreamCodec<>() {
			@Override
			public DataMapValue decode(RegistryFriendlyByteBuf buf) {
				var id = buf.readUtf();
				var key = synced.get(id);

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
				VLStreamCodecs.encode(buf, dataMapValue.key().type(), dataMapValue.value());
			}
		};

		this.valueListStreamCodec = KLibStreamCodecs.listOf(valueStreamCodec);
	}

	public <T> DataKey.Builder<T> builder(String id, DataType<T> type, T defaultValue) {
		return new DataKey.Builder<>(this, id, type, defaultValue);
	}

	public <T> DataKey.Builder<T> buildDefault(String id, DataType<T> type, @Nullable T defaultValue, @Nullable ImBuilderType<T> imBuilder) {
		return builder(id, type, defaultValue).imBuilder(imBuilder).save().sync();
	}

	public <T> DataKey<T> createDefault(String id, DataType<T> type, @Nullable T defaultValue, @Nullable ImBuilderType<T> imBuilder) {
		return buildDefault(id, type, defaultValue, imBuilder).build();
	}

	public DataKey<Boolean> createBoolean(String id, boolean defaultValue) {
		return createDefault(id, DataTypes.BOOL, defaultValue, BooleanImBuilder.TYPE);
	}

	public DataKey<Integer> createInt(String id, int defaultValue) {
		return createDefault(id, DataTypes.VAR_INT, defaultValue, IntImBuilder.TYPE_1M);
	}

	public DataKey<Integer> createInt(String id, int defaultValue, int min, int max) {
		return createDefault(id, DataTypes.VAR_INT, defaultValue, () -> new IntImBuilder(min, max));
	}

	public DataKey<Integer> createTicks(String id, int defaultValue) {
		return createDefault(id, DataTypes.TICKS, defaultValue, IntImBuilder.TYPE_1M);
	}

	public DataKey<Integer> createTicks(String id, int defaultValue, int min, int max) {
		return createDefault(id, DataTypes.TICKS, defaultValue, () -> new IntImBuilder(min, max));
	}

	public DataKey<Float> createFloat(String id, float defaultValue) {
		return createDefault(id, DataTypes.FLOAT, defaultValue, FloatImBuilder.TYPE);
	}

	public DataKey<Float> createFloat(String id, float defaultValue, float min, float max) {
		return createDefault(id, DataTypes.FLOAT, defaultValue, FloatImBuilder.type(min, max));
	}

	public DataKey<Double> createDouble(String id, double defaultValue) {
		return createDefault(id, DataTypes.DOUBLE, defaultValue, DoubleImBuilder.TYPE);
	}

	public DataKey<Double> createDouble(String id, double defaultValue, double min, double max) {
		return createDefault(id, DataTypes.DOUBLE, defaultValue, DoubleImBuilder.type(min, max));
	}

	public <E> DataKey<E> createEnum(String id, DataType<E> type, E defaultValue, E[] values) {
		return createDefault(id, type, defaultValue, () -> new EnumImBuilder<>(values));
	}

	@Override
	public String toString() {
		return name;
	}
}
