package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTypeStorage {
	public final String name;
	public final boolean alwaysSyncToAllClients;
	public final Map<ResourceLocation, DataType<?>> saved;
	final Map<ResourceLocation, DataType<?>> synced;
	public final StreamCodec<RegistryFriendlyByteBuf, DataMapValue> valueStreamCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, List<DataMapValue>> valueListStreamCodec;

	public DataTypeStorage(String name, boolean alwaysSyncToAllClients) {
		this.name = name;
		this.alwaysSyncToAllClients = alwaysSyncToAllClients;
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

				return new DataMapValue(type, type.streamCodec().decode(buf));
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, DataMapValue value) {
				buf.writeResourceLocation(value.type().id());
				value.type().streamCodec().encode(buf, Cast.to(value.value()));
			}
		};

		this.valueListStreamCodec = valueStreamCodec.list();
	}

	public <T> DataType.Builder<T> builder(ResourceLocation id, T defaultValue) {
		return new DataType.Builder<>(this, id, defaultValue);
	}

	@ApiStatus.Internal
	public <T> DataType.Builder<T> internal(String id, T defaultValue) {
		return builder(Shimmer.id(id), defaultValue);
	}

	public <T> DataType.Builder<T> video(String id, T defaultValue) {
		return builder(ResourceLocation.fromNamespaceAndPath("video", id), defaultValue);
	}

	@Override
	public String toString() {
		return name;
	}
}
