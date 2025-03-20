package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataTypeStorage {
	public final String name;
	public final boolean alwaysSyncToAllClients;
	public final Map<ResourceLocation, DataType<?>> all;
	public final Map<ResourceLocation, DataType<?>> saved;
	final Map<ResourceLocation, DataType<?>> synced;
	public final StreamCodec<RegistryFriendlyByteBuf, DataMapValue> valueStreamCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, List<DataMapValue>> valueListStreamCodec;

	public DataTypeStorage(String name, boolean alwaysSyncToAllClients) {
		this.name = name;
		this.alwaysSyncToAllClients = alwaysSyncToAllClients;
		this.all = new LinkedHashMap<>();
		this.saved = new HashMap<>();
		this.synced = new HashMap<>();

		this.valueStreamCodec = new StreamCodec<>() {
			@Override
			public DataMapValue decode(RegistryFriendlyByteBuf buf) {
				var id = buf.readResourceLocation();
				var type = synced.get(id);

				if (type == null) {
					throw new NullPointerException("Data type with id " + id + " not found. Available types in '" + DataTypeStorage.this.name + "': " + synced.keySet());
				}

				return new DataMapValue(type, type.type().streamCodec().decode(buf));
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, DataMapValue value) {
				buf.writeResourceLocation(value.type().id());
				value.type().type().streamCodec().encode(buf, Cast.to(value.value()));
			}
		};

		this.valueListStreamCodec = valueStreamCodec.list();
	}

	public <T> DataType.Builder<T> builder(ResourceLocation id, @Nullable KnownCodec<T> type, T defaultValue) {
		return new DataType.Builder<>(this, id, type, defaultValue);
	}

	@ApiStatus.Internal
	public <T> DataType.Builder<T> internal(String id, @Nullable KnownCodec<T> type, T defaultValue) {
		return builder(Shimmer.id(id), type, defaultValue);
	}

	public <T> DataType.Builder<T> video(String id, @Nullable KnownCodec<T> type, T defaultValue) {
		return builder(ResourceLocation.fromNamespaceAndPath("video", id), type, defaultValue);
	}

	@Override
	public String toString() {
		return name;
	}
}
