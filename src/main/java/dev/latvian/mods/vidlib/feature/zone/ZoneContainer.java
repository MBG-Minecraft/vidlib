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
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
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

public class ZoneContainer implements CustomRegistryValue<RegistryFriendlyByteBuf, ZoneContainer>, ZoneLike, Comparable<ZoneContainer> {
	public static final DynamicType<RegistryFriendlyByteBuf, ZoneContainer> TYPE = DynamicType.create("default",
		RecordCodecBuilder.mapCodec(i -> i.group(
			Ref.<ZoneContainer>contextRefCodec().forGetter(ZoneContainer::ref),
			MapCodec.unit(false).forGetter(z -> z.generated),
			MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(z -> z.dimension),
			Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(z -> List.copyOf(z.tags)),
			ZoneVolume.CODEC.listOf().fieldOf("zones").forGetter(z -> z.zones.stream().map(zi -> zi.volume).toList())
		).apply(i, ZoneContainer::new)),
		CompositeStreamCodec.of(
			Ref.contextRefStreamCodec(), ZoneContainer::ref,
			ByteBufCodecs.BOOL, z -> z.generated,
			KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), z -> z.dimension,
			KLibStreamCodecs.listOf(ByteBufCodecs.STRING_UTF8), z -> List.copyOf(z.tags),
			KLibStreamCodecs.listOf(ZoneVolume.STREAM_CODEC), z -> z.zones.stream().map(zi -> zi.volume).toList(),
			ZoneContainer::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, ZoneContainer> REGISTRY = CustomRegistry.create("zone_container", TYPE);

	public static final Codec<Ref<ZoneContainer>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<ZoneContainer>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<ZoneContainer>> DATA_TYPE = REGISTRY.dataType();

	public final Ref<ZoneContainer> ref;
	public final ResourceKey<Level> dimension;
	public final List<Zone> zones;
	public final Set<String> tags;
	public int priority;
	public final Int2ObjectOpenHashMap<List<Zone>> entityZones;
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

	private ZoneContainer(Ref<ZoneContainer> ref, boolean generated, ResourceKey<Level> dimension, List<String> tags, List<ZoneVolume> zones) {
		this(ref, dimension);
		this.generated = generated;
		this.tags.addAll(tags);

		for (var zone : zones) {
			add(zone);
		}
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, ZoneContainer> getRegistry() {
		return REGISTRY;
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, ZoneContainer> type() {
		return TYPE;
	}

	public ZoneContainer add(ZoneVolume zone) {
		var instance = zone.shape().value().createInstance(this, zone);
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
	public Zone getFirst(Vec3 pos) {
		for (var instance : zones) {
			if (instance.volume.shape().value().contains(pos)) {
				return instance;
			}
		}

		return null;
	}

	public List<Zone> getAll(Vec3 pos) {
		var list = new ArrayList<Zone>(1);

		for (var instance : zones) {
			if (instance.volume.shape().value().contains(pos)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Nullable
	public Zone getFirst(AABB box) {
		for (var instance : zones) {
			if (instance.volume.shape().value().intersects(box)) {
				return instance;
			}
		}

		return null;
	}

	public List<Zone> getAll(AABB box) {
		var list = new ArrayList<Zone>(1);

		for (var instance : zones) {
			if (instance.volume.shape().value().intersects(box)) {
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

	public void update(int index, ZoneVolume zoneVolume) {
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
				var box = instance.volume.shape().value().toAABB();
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
				var box = instance.volume.shape().value().toAAIBB();
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
	public ZoneClipResult clip(ClipContext ctx) {
		if (zones.isEmpty()) {
			return null;
		} else if (zones.size() == 1) {
			var instance = zones.getFirst();
			return instance.volume.shape().value().clip(new ZoneClipContext(instance, ctx));
		}

		ZoneClipResult result = null;

		for (var instance : zones) {
			var clip = instance.volume.shape().value().clip(new ZoneClipContext(instance, ctx));

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

		if (toAABB().contains(x, y, z)) {
			if (zones.size() == 1) {
				return zones.getFirst().volume.shape().value().contains(x, y, z);
			}

			for (var instance : zones) {
				if (instance.volume.shape().value().contains(x, y, z)) {
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
				return zones.getFirst().volume.shape().value().contains(x, y, z);
			}

			for (var instance : zones) {
				if (instance.volume.shape().value().contains(x, y, z)) {
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
			return zones.getFirst().volume.shape().value().intersects(box);
		}

		for (var instance : zones) {
			if (instance.volume.shape().value().intersects(box)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		if (zones.size() == 1) {
			return zones.getFirst().volume.shape().value().getBlocks();
		}

		var stream = Stream.<BlockPos>empty();

		for (var instance : zones) {
			stream = Stream.concat(stream, instance.volume.shape().value().getBlocks());
		}

		return stream;
	}

	@Override
	public VoxelShape createVoxelShape() {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().volume.shape().value().createVoxelShape();

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).volume.shape().value().createVoxelShape());
		}

		return shape;
	}

	@Override
	public VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		if (zones.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zones.getFirst().volume.shape().value().createBlockRenderingShape(predicate);

		for (int i = 1; i < zones.size(); i++) {
			shape = Shapes.or(shape, zones.get(i).volume.shape().value().createBlockRenderingShape(predicate));
		}

		return shape;
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		if (zones.size() == 1) {
			return zones.getFirst().volume.shape().value().closestDistanceTo(pos);
		}

		var dist = Double.POSITIVE_INFINITY;

		for (var instance : zones) {
			dist = Math.min(dist, instance.volume.shape().value().closestDistanceTo(pos));

			if (dist <= 0D) {
				return 0D;
			}
		}

		return dist;
	}
}
