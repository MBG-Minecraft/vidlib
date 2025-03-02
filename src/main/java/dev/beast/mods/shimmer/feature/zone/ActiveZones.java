package dev.beast.mods.shimmer.feature.zone;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActiveZones implements Iterable<ZoneContainer> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ActiveZones> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ActiveZones decode(RegistryFriendlyByteBuf buf) {
			int count = buf.readVarInt();
			var active = new ActiveZones();

			for (int i = 0; i < count; i++) {
				var container = ZoneContainer.STREAM_CODEC.decode(buf);
				active.containers.put(container.id, container);
			}

			return active;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ActiveZones value) {
			buf.writeVarInt(value.containers.size());

			for (var container : value) {
				ZoneContainer.STREAM_CODEC.encode(buf, container);
			}
		}
	};

	public final Map<ResourceLocation, ZoneContainer> containers;
	public final Int2ObjectOpenHashMap<List<ZoneInstance>> entityZones;

	public ActiveZones() {
		this.containers = new LinkedHashMap<>();
		this.entityZones = new Int2ObjectOpenHashMap<>();
	}

	@Nullable
	public ZoneContainer get(ResourceLocation id) {
		return containers.get(id);
	}

	public void filter(ResourceKey<Level> dimension, ActiveZones from) {
		containers.clear();
		entityZones.clear();

		for (var container : from) {
			if (container.dimension == dimension) {
				containers.put(container.id, container);
			}
		}
	}

	public void update(Collection<ZoneContainer> zones) {
		containers.clear();

		for (var zone : zones) {
			containers.put(zone.id, zone);
		}
	}

	@Override
	@NotNull
	public Iterator<ZoneContainer> iterator() {
		return containers.values().iterator();
	}

	@Nullable
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
		ZoneClipResult result = null;

		for (var container : this) {
			var clip = container.clip(start, end);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}
}