package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
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

public class ZoneContainer implements CustomRegistryValue<RegistryFriendlyByteBuf, ZoneContainer>, VolumeLike, Comparable<ZoneContainer> {
	public static final DynamicType<RegistryFriendlyByteBuf, ZoneContainer> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(i -> i.group(
			Ref.<ZoneContainer>contextRefCodec().forGetter(ZoneContainer::ref),
			MapCodec.unit(false).forGetter(z -> z.generated),
			MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(z -> z.dimension),
			Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(z -> List.copyOf(z.tags)),
			Zone.CODEC.listOf().fieldOf("volumes").forGetter(z -> z.zones.stream().map(zi -> zi.zone).toList()),
			Codec.INT.optionalFieldOf("priority", 0).forGetter(z -> z.priority)
		).apply(i, ZoneContainer::new)),
		CompositeStreamCodec.of(
			Ref.contextRefStreamCodec(), ZoneContainer::ref,
			ByteBufCodecs.BOOL, z -> z.generated,
			KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), z -> z.dimension,
			KLibStreamCodecs.listOf(ByteBufCodecs.STRING_UTF8), z -> List.copyOf(z.tags),
			KLibStreamCodecs.listOf(Zone.STREAM_CODEC), z -> z.zones.stream().map(zi -> zi.zone).toList(),
			ByteBufCodecs.INT, z -> z.priority,
			ZoneContainer::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, ZoneContainer> REGISTRY = CustomRegistry.create("zone_container", TYPE);

	public static final Codec<Ref<ZoneContainer>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<ZoneContainer>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<ZoneContainer>> DATA_TYPE = REGISTRY.dataType();

	public final Ref<ZoneContainer> ref;
	public final ResourceKey<Level> dimension;
	public final List<ResolvedZone> zones;
	public final Set<String> tags;
	public int priority;
	public final Int2ObjectOpenHashMap<List<ResolvedZone>> entityZones;
	boolean generated;
	private AABB boundingBox;
	private AAIBB intBoundingBox;

	public ZoneContainer(Ref<ZoneContainer> ref, ResourceKey<Level> dimension) {
		this.ref = ref;
		this.dimension = dimension;
		this.zones = new ArrayList<>();
		this.tags = new LinkedHashSet<>();
		this.priority = 0;
		this.entityZones = new Int2ObjectOpenHashMap<>();
		this.generated = false;
		this.boundingBox = null;
		this.intBoundingBox = null;
	}

	private ZoneContainer(Ref<ZoneContainer> ref, boolean generated, ResourceKey<Level> dimension, List<String> tags, List<Zone> zones, int priority) {
		this(ref, dimension);
		this.generated = generated;
		this.tags.addAll(tags);

		for (var zone : zones) {
			add(zone);
		}

		this.priority = priority;
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, ZoneContainer> getRegistry() {
		return REGISTRY;
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ZoneContainer> type() {
		return TYPE;
	}

	public ZoneContainer add(Zone zone) {
		var instance = new ResolvedZone(this, zone);
		instance.index = zones.size();

		instance.tags.add(ref.key());
		instance.tags.addAll(tags);
		instance.tags.addAll(zone.tags());

		zones.add(instance);

		boundingBox = null;
		intBoundingBox = null;
		return this;
	}

	public void tick(ZoneCache zoneCache, @Nullable Level level) {
		entityZones.clear();

		for (var instance : zones) {
			instance.tick(level);
		}

		for (var entry : entityZones.int2ObjectEntrySet()) {
			var list = zoneCache.entityZones.get(entry.getIntKey());

			if (list == null) {
				list = new ArrayList<>(entry.getValue().size());
				zoneCache.entityZones.put(entry.getIntKey(), list);
			}

			list.addAll(entry.getValue());
		}
	}

	@Nullable
	public ResolvedZone getFirst(Vec3 pos) {
		for (var instance : zones) {
			if (instance.zone.volume().value().contains(pos)) {
				return instance;
			}
		}

		return null;
	}

	public List<ResolvedZone> getAll(Vec3 pos) {
		var list = new ArrayList<ResolvedZone>(1);

		for (var instance : zones) {
			if (instance.zone.volume().value().contains(pos)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Nullable
	public ResolvedZone getFirst(AABB box) {
		for (var instance : zones) {
			if (instance.zone.volume().value().intersects(box)) {
				return instance;
			}
		}

		return null;
	}

	public List<ResolvedZone> getAll(AABB box) {
		var list = new ArrayList<ResolvedZone>(1);

		for (var instance : zones) {
			if (instance.zone.volume().value().intersects(box)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Override
	public int compareTo(@NotNull ZoneContainer container) {
		int i = Integer.compare(container.priority, priority);
		return i == 0 ? ref.key().compareTo(container.ref.key()) : i;
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
		intBoundingBox = null;
	}

	public void update(int index, Zone zone) {
	}

	public boolean isGenerated() {
		return generated;
	}

	// Helper methods //

	@Override
	public AABB toAABB() {
		if (boundingBox == null) {
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double minZ = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double maxZ = Double.NEGATIVE_INFINITY;

			for (var instance : zones) {
				var box = instance.zone.volume().value().box();
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

	@Override
	public AAIBB toAAIBB() {
		if (intBoundingBox == null) {
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			int maxZ = Integer.MIN_VALUE;

			for (var instance : zones) {
				var box = instance.zone.volume().value().intBox();
				minX = Math.min(minX, box.minX());
				minY = Math.min(minY, box.minY());
				minZ = Math.min(minZ, box.minZ());
				maxX = Math.max(maxX, box.maxX());
				maxY = Math.max(maxY, box.maxY());
				maxZ = Math.max(maxZ, box.maxZ());
			}

			intBoundingBox = new AAIBB(minX, minY, minZ, maxX, maxY, maxZ);
		}

		return intBoundingBox;
	}

	@Nullable
	public ZoneHit hit(HitBuilder hit) {
		ZoneHit result = null;

		for (var zone : zones) {
			zone.zone.volume().value().hit(hit);

			if (hit.hit != null && (result == null || hit.distanceSq < result.distanceSq())) {
				result = new ZoneHit(zone, hit.hit, hit.distanceSq, hit.direction);
			}
		}

		return result;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		if (zones.isEmpty()) {
			return false;
		}

		if (toAABB().contains(x, y, z)) {
			if (zones.size() == 1) {
				return zones.getFirst().zone.volume().value().contains(x, y, z);
			}

			for (var instance : zones) {
				if (instance.zone.volume().value().contains(x, y, z)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean contains(int x, int y, int z) {
		if (zones.isEmpty()) {
			return false;
		}

		if (toAABB().contains(x, y, z)) {
			if (zones.size() == 1) {
				return zones.getFirst().zone.volume().value().contains(x, y, z);
			}

			for (var instance : zones) {
				if (instance.zone.volume().value().contains(x, y, z)) {
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
			return zones.getFirst().zone.volume().value().intersects(box);
		}

		for (var instance : zones) {
			if (instance.zone.volume().value().intersects(box)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		if (zones.size() == 1) {
			return zones.getFirst().zone.volume().value().getBlocks();
		}

		var stream = Stream.<BlockPos>empty();

		for (var instance : zones) {
			stream = Stream.concat(stream, instance.zone.volume().value().getBlocks());
		}

		return stream;
	}

	@Override
	public VoxelShape createVoxelShape() {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().zone.volume().value().createVoxelShape();

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).zone.volume().value().createVoxelShape());
		}

		return shape;
	}

	@Override
	public VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().zone.volume().value().createBlockRenderingShape(predicate);

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).zone.volume().value().createBlockRenderingShape(predicate));
		}

		return shape;
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		if (zones.size() == 1) {
			return zones.getFirst().zone.volume().value().closestDistanceTo(pos);
		}

		var dist = Double.POSITIVE_INFINITY;

		for (var instance : zones) {
			dist = Math.min(dist, instance.zone.volume().value().closestDistanceTo(pos));

			if (dist <= 0D) {
				return 0D;
			}
		}

		return dist;
	}
}
