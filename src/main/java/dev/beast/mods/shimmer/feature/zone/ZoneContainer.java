package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.Line;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ZoneContainer {
	public static final StreamCodec<RegistryFriendlyByteBuf, ZoneContainer> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ZoneContainer decode(RegistryFriendlyByteBuf buf) {
			var id = ResourceLocation.STREAM_CODEC.decode(buf);
			var dimension = ShimmerStreamCodecs.DIMENSION.decode(buf);
			var container = new ZoneContainer(id, dimension);

			int tags = buf.readVarInt();

			for (int i = 0; i < tags; i++) {
				container.tags.add(buf.readUtf());
			}

			int count = buf.readVarInt();

			for (int i = 0; i < count; i++) {
				container.add(Zone.STREAM_CODEC.decode(buf));
			}

			return container;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ZoneContainer value) {
			ResourceLocation.STREAM_CODEC.encode(buf, value.id);
			ShimmerStreamCodecs.DIMENSION.encode(buf, value.dimension);

			buf.writeVarInt(value.tags.size());

			for (var tag : value.tags) {
				buf.writeUtf(tag);
			}

			buf.writeVarInt(value.zones.size());

			for (var zone : value.zones) {
				Zone.STREAM_CODEC.encode(buf, zone.zone);
			}
		}
	};

	ActiveZones parent;
	public final ResourceLocation id;
	public final ResourceKey<Level> dimension;
	public final List<ZoneInstance> zones;
	public boolean hasPlayerOverrides;
	public final Set<String> tags;
	public final Int2ObjectOpenHashMap<List<ZoneInstance>> entityZones;

	public ZoneContainer(ResourceLocation id, ResourceKey<Level> dimension) {
		this.id = id;
		this.dimension = dimension;
		this.zones = new ArrayList<>();
		this.hasPlayerOverrides = false;
		this.tags = new LinkedHashSet<>();
		this.entityZones = new Int2ObjectOpenHashMap<>();
	}

	public ZoneContainer add(Zone zone) {
		var instance = zone.shape().createInstance(this, zone);
		instance.index = zones.size();

		instance.tags.add(id.toString());
		instance.tags.addAll(tags);
		instance.tags.addAll(zone.tags());

		zones.add(instance);

		if (!zone.playerOverrides().isEmpty()) {
			hasPlayerOverrides = true;
		}

		if (parent != null) {
			parent.solidZones = null;
		}

		return this;
	}

	public void remove(int index) {
		zones.remove(index);
		hasPlayerOverrides = false;

		for (int i = index; i < zones.size(); i++) {
			var zone = zones.get(i);
			zone.index = i;

			if (!zone.zone.playerOverrides().isEmpty()) {
				hasPlayerOverrides = true;
			}
		}
	}

	public void tick(ActiveZones activeZones, @Nullable Level level) {
		entityZones.clear();

		for (var instance : zones) {
			instance.tick(level);
		}

		for (var entry : entityZones.int2ObjectEntrySet()) {
			var list = activeZones.entityZones.get(entry.getIntKey());

			if (list == null) {
				list = new ArrayList<>(entry.getValue().size());
				activeZones.entityZones.put(entry.getIntKey(), list);
			}

			list.addAll(entry.getValue());
		}
	}

	@Nullable
	public ZoneClipResult clip(Line ray) {
		ZoneClipResult result = null;

		for (var instance : zones) {
			var clip = instance.zone.shape().clip(instance, ray);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}

	@Nullable
	public ZoneInstance getFirst(Vec3 pos) {
		for (var instance : zones) {
			if (instance.zone.shape().contains(pos)) {
				return instance;
			}
		}

		return null;
	}

	public List<ZoneInstance> getAll(Vec3 pos) {
		var list = new ArrayList<ZoneInstance>(1);

		for (var instance : zones) {
			if (instance.zone.shape().contains(pos)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Nullable
	public ZoneInstance getFirst(AABB box) {
		for (var instance : zones) {
			if (instance.zone.shape().intersects(box)) {
				return instance;
			}
		}

		return null;
	}

	public List<ZoneInstance> getAll(AABB box) {
		var list = new ArrayList<ZoneInstance>(1);

		for (var instance : zones) {
			if (instance.zone.shape().intersects(box)) {
				list.add(instance);
			}
		}

		return list;
	}
}
