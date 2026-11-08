package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefType;
import dev.latvian.mods.klib.texture.CubeTexturePatch;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.klib.volume.BlockVolume;
import dev.latvian.mods.klib.volume.JoinedVolume;
import dev.latvian.mods.klib.volume.Volume;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public record Zone(
	int priority,
	boolean generated,
	ResourceKey<Level> dimension,
	Ref<Volume> volume,
	Ref<EntityFilter> entityFilter,
	CompoundTag data,
	Ref<EntityFilter> solid,
	List<ZoneTag> tags,
	boolean anchor,
	Optional<ZoneFluid> fluid,
	ZoneDisplayInfo display
) implements CustomRegistryValue<RegistryFriendlyByteBuf, Zone> {
	public static final DynamicType<RegistryFriendlyByteBuf, Zone> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.intRange(-128, 127).optionalFieldOf("priority", 0).forGetter(Zone::priority),
			MapCodec.unit(Boolean.FALSE).forGetter(Zone::generated),
			MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Zone::dimension),
			Volume.CODEC.optionalFieldOf("volume", Volume.UNIVERSE).forGetter(Zone::volume),
			EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER).forGetter(Zone::entityFilter),
			CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(Zone::data),
			EntityFilter.CODEC.optionalFieldOf("solid", EntityFilter.NONE).forGetter(Zone::solid),
			ZoneTag.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(Zone::tags),
			Codec.BOOL.optionalFieldOf("anchor", false).forGetter(Zone::anchor),
			ZoneFluid.CODEC.optionalFieldOf("fluid").forGetter(Zone::fluid),
			ZoneDisplayInfo.CODEC.optionalFieldOf("display", ZoneDisplayInfo.DEFAULT).forGetter(Zone::display)
		).apply(instance, Zone::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.INT8, Zone::priority,
			ByteBufCodecs.BOOL, Zone::generated,
			MCStreamCodecs.DIMENSION, Zone::dimension,
			Volume.STREAM_CODEC, Zone::volume,
			EntityFilter.STREAM_CODEC, Zone::entityFilter,
			MCStreamCodecs.COMPOUND_TAG, Zone::data,
			KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.NONE), Zone::solid,
			KLibStreamCodecs.listOf(ZoneTag.STREAM_CODEC), Zone::tags,
			ByteBufCodecs.BOOL, Zone::anchor,
			ByteBufCodecs.optional(ZoneFluid.STREAM_CODEC), Zone::fluid,
			ZoneDisplayInfo.STREAM_CODEC, Zone::display,
			Zone::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Zone> REGISTRY = CustomRegistry.create("zone", TYPE);
	public static final Codec<Ref<Zone>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Zone>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<Zone>> DATA_TYPE = REGISTRY.dataType();

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int priority = 0;
		private ResourceKey<Level> dimension = Level.OVERWORLD;
		private final List<Ref<Volume>> volumes = new ArrayList<>(1);
		private Color color = Color.CYAN;
		private Ref<EntityFilter> entityFilter = EntityFilter.PLAYER;
		private CompoundTag data = new CompoundTag();
		private Ref<EntityFilter> solid = EntityFilter.NONE;
		private final List<ZoneTag> tags = new ArrayList<>(0);
		private boolean anchor = false;
		private ZoneFluid fluid = null;
		private CubeTexturePatch textures = CubeTexturePatch.EMPTY;
		private ZoneFog fog = ZoneFog.NONE;

		public Zone build() {
			var display = new ZoneDisplayInfo(
				color,
				textures,
				fog
			);

			return new Zone(
				priority,
				true,
				dimension,
				volumes.isEmpty() ? Volume.UNIVERSE : volumes.size() == 1 ? volumes.getFirst() : JoinedVolume.create(List.copyOf(volumes)).ref(),
				entityFilter,
				data.isEmpty() ? Empty.COMPOUND_TAG : data.copy(),
				solid,
				List.copyOf(tags),
				anchor,
				Optional.ofNullable(fluid),
				display.equals(ZoneDisplayInfo.DEFAULT) ? ZoneDisplayInfo.DEFAULT : display
			);
		}

		public Builder priority(int priority) {
			this.priority = priority;
			return this;
		}

		public Builder dimension(ResourceKey<Level> dimension) {
			this.dimension = dimension;
			return this;
		}

		public Builder volume(Ref<Volume> volume) {
			this.volumes.add(volume);
			return this;
		}

		public Builder volume(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			return volume(BlockVolume.of(new AAIBB(minX, minY, minZ, maxX, maxY, maxZ)).ref());
		}

		public Builder color(Color color) {
			this.color = color;
			return this;
		}

		public Builder entityFilter(Ref<EntityFilter> entityFilter) {
			this.entityFilter = entityFilter;
			return this;
		}

		public Builder resetData() {
			this.data = new CompoundTag();
			return this;
		}

		public Builder data(CompoundTag data) {
			this.data.merge(data);
			return this;
		}

		public Builder data(String key, int value) {
			var tag = new CompoundTag();
			tag.putInt(key, value);
			return data(tag);
		}

		public Builder data(String key, double value) {
			var tag = new CompoundTag();
			tag.putDouble(key, value);
			return data(tag);
		}

		public Builder solid(Ref<EntityFilter> solid) {
			this.solid = solid;
			return this;
		}

		public Builder tag(ZoneTag tag) {
			this.tags.add(tag);
			return this;
		}

		public Builder anchor(boolean anchor) {
			this.anchor = anchor;
			return this;
		}

		public Builder fluid(@Nullable ZoneFluid fluid) {
			this.fluid = fluid;
			return this;
		}

		public Builder textures(CubeTexturePatch textures) {
			this.textures = textures;
			return this;
		}

		public Builder fog(ZoneFog fog) {
			this.fog = fog;
			return this;
		}
	}

	public static class Loader extends JsonRegistryReloadListener<Zone> {
		public Loader(RefType refType) {
			super("vidlib/zone", Zone.REGISTRY, refType);
		}

		@Override
		public void generate(BiConsumer<String, Zone> callback) {
			super.generate(callback);
			VLPlatformHelper.INSTANCE.generateZones(callback, refType);
		}
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, Zone> getRegistry() {
		return REGISTRY;
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, Zone> type() {
		return TYPE;
	}
}
