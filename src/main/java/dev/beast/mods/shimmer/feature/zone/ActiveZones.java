package dev.beast.mods.shimmer.feature.zone;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActiveZones implements Iterable<ZoneContainer> {
	public static final ActiveZones SERVER = new ActiveZones();
	public static final ActiveZones CLIENT = new ActiveZones();

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
}