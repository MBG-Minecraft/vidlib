package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ZoneContainer implements ZoneLike, Comparable<ZoneContainer> {
	private static final int FLAG_GENERATED = 1;
	private static final int FLAG_NOT_OVERWORLD = 2;
	private static final int FLAG_HAS_TAGS = 4;

	public static final StreamCodec<RegistryFriendlyByteBuf, ZoneContainer> DIRECT_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ZoneContainer decode(RegistryFriendlyByteBuf buf) {
			var id = ResourceLocation.STREAM_CODEC.decode(buf);
			int flags = buf.readVarInt();

			var dimension = ((flags & FLAG_NOT_OVERWORLD) != 0) ? MCStreamCodecs.DIMENSION.decode(buf) : Level.OVERWORLD;
			var container = new ZoneContainer(id, dimension);

			container.generated = (flags & FLAG_GENERATED) != 0;

			if ((flags & FLAG_HAS_TAGS) != 0) {
				int tags = buf.readVarInt();

				for (int i = 0; i < tags; i++) {
					container.tags.add(buf.readUtf());
				}
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

			int flags = 0;

			if (value.generated) {
				flags |= FLAG_GENERATED;
			}

			if (value.dimension != Level.OVERWORLD) {
				flags |= FLAG_NOT_OVERWORLD;
			}

			if (!value.tags.isEmpty()) {
				flags |= FLAG_HAS_TAGS;
			}

			buf.writeVarInt(flags);

			if (value.dimension != Level.OVERWORLD) {
				MCStreamCodecs.DIMENSION.encode(buf, value.dimension);
			}

			if (!value.tags.isEmpty()) {
				buf.writeVarInt(value.tags.size());

				for (var tag : value.tags) {
					buf.writeUtf(tag);
				}
			}

			buf.writeVarInt(value.zones.size());

			for (var zone : value.zones) {
				Zone.STREAM_CODEC.encode(buf, zone.zone);
			}
		}
	};

	public static final VLRegistry<ZoneContainer> REGISTRY = VLRegistry.createServer("zone_container", ZoneContainer.class);
	public static final DataType<ZoneContainer> DATA_TYPE = REGISTRY.dataType();
	public static final CommandDataType<ZoneContainer> COMMAND = CommandDataType.of(DATA_TYPE);

	ActiveZones parent;
	public final ResourceLocation id;
	public final ResourceKey<Level> dimension;
	public final List<ZoneInstance> zones;
	public final Set<String> tags;
	public int priority;
	public final Int2ObjectOpenHashMap<List<ZoneInstance>> entityZones;
	boolean generated;
	private AABB boundingBox;

	public ZoneContainer(ResourceLocation id, ResourceKey<Level> dimension) {
		this.id = id;
		this.dimension = dimension;
		this.zones = new ArrayList<>();
		this.tags = new LinkedHashSet<>();
		this.priority = 0;
		this.entityZones = new Int2ObjectOpenHashMap<>();
		this.generated = false;
		this.boundingBox = null;
	}

	public ZoneContainer add(Zone zone) {
		var instance = zone.shape().createInstance(this, zone);
		instance.index = zones.size();

		instance.tags.add(id.toString());
		instance.tags.addAll(tags);
		instance.tags.addAll(zone.tags());

		zones.add(instance);

		if (parent != null) {
			parent.solidZones = null;
		}

		boundingBox = null;
		return this;
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

	@Override
	public int compareTo(@NotNull ZoneContainer container) {
		int i = Integer.compare(container.priority, priority);
		return i == 0 ? id.compareTo(container.id) : i;
	}

	@ApiStatus.Internal
	public void remove(int index) {
		if (index < 0 || index >= zones.size()) {
			return;
		}

		zones.remove(index);

		for (int i = 0; i < zones.size(); i++) {
			var zone = zones.get(i);
			zone.index = i;
		}

		boundingBox = null;
	}

	public void update(int index, Zone zoneData) {
	}

	public boolean isGenerated() {
		return generated;
	}

	// Helper methods //


	@Override
	public AABB getBoundingBox() {
		if (zones.size() == 1) {
			return zones.getFirst().zone.shape().getBoundingBox();
		}

		if (boundingBox == null) {
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double minZ = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double maxZ = Double.NEGATIVE_INFINITY;

			for (var instance : zones) {
				var box = instance.zone.shape().getBoundingBox();
				minX = Math.min(minX, box.minX);
				minY = Math.min(minY, box.minY);
				minZ = Math.min(minZ, box.minZ);
				maxX = Math.max(maxX, box.maxX);
				maxY = Math.max(maxY, box.maxY);
				maxZ = Math.max(maxZ, box.maxZ);
			}

			boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		}

		return boundingBox;
	}

	@Nullable
	public ZoneClipResult clip(ClipContext ctx) {
		if (zones.isEmpty()) {
			return null;
		} else if (zones.size() == 1) {
			var instance = zones.getFirst();
			return instance.zone.shape().clip(instance, ctx);
		}

		ZoneClipResult result = null;

		for (var instance : zones) {
			var clip = instance.zone.shape().clip(instance, ctx);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		if (zones.isEmpty()) {
			return false;
		}

		if (getBoundingBox().contains(x, y, z)) {
			if (zones.size() == 1) {
				return zones.getFirst().zone.shape().contains(x, y, z);
			}

			for (var instance : zones) {
				if (instance.zone.shape().contains(x, y, z)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean intersects(AABB box) {
		if (zones.isEmpty()) {
			return false;
		} else if (zones.size() == 1) {
			return zones.getFirst().zone.shape().intersects(box);
		}

		for (var instance : zones) {
			if (instance.zone.shape().intersects(box)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		if (zones.size() == 1) {
			return zones.getFirst().zone.shape().getBlocks();
		}

		var stream = Stream.<BlockPos>empty();

		for (var instance : zones) {
			stream = Stream.concat(stream, instance.zone.shape().getBlocks());
		}

		return stream;
	}

	@Override
	public VoxelShape createVoxelShape() {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().zone.shape().createVoxelShape();

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).zone.shape().createVoxelShape());
		}

		return shape;
	}

	@Override
	public VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().zone.shape().createBlockRenderingShape(predicate);

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).zone.shape().createBlockRenderingShape(predicate));
		}

		return shape;
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		if (zones.size() == 1) {
			return zones.getFirst().zone.shape().closestDistanceTo(pos);
		}

		var dist = Double.POSITIVE_INFINITY;

		for (var instance : zones) {
			dist = Math.min(dist, instance.zone.shape().closestDistanceTo(pos));

			if (dist <= 0D) {
				return 0D;
			}
		}

		return dist;
	}
}
