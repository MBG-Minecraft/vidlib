package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ReplayMarkerData(
	ReplayMarkerGroup group,
	Optional<Color> color,
	ResourceKey<Level> dimension,
	Optional<Vec3> position,
	String description,
	Optional<Tag> customData
) {
	public static final Codec<ReplayMarkerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ReplayMarkerGroup.CODEC.optionalFieldOf("group", ReplayMarkerGroup.DEFAULT).forGetter(ReplayMarkerData::group),
		Color.SOLID_CODEC.optionalFieldOf("color").forGetter(ReplayMarkerData::color),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ReplayMarkerData::dimension),
		MCCodecs.VEC3.optionalFieldOf("position").forGetter(ReplayMarkerData::position),
		Codec.STRING.optionalFieldOf("description", "").forGetter(ReplayMarkerData::description),
		ExtraCodecs.converter(NbtOps.INSTANCE).optionalFieldOf("custom_data").forGetter(ReplayMarkerData::customData)
	).apply(instance, ReplayMarkerData::new));

	public static final StreamCodec<ByteBuf, ReplayMarkerData> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.optional(ReplayMarkerGroup.STREAM_CODEC, ReplayMarkerGroup.DEFAULT), ReplayMarkerData::group,
		ByteBufCodecs.optional(Color.SOLID_STREAM_CODEC), ReplayMarkerData::color,
		KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), ReplayMarkerData::dimension,
		ByteBufCodecs.optional(MCStreamCodecs.VEC3), ReplayMarkerData::position,
		ByteBufCodecs.STRING_UTF8, ReplayMarkerData::description,
		ByteBufCodecs.optional(ByteBufCodecs.TAG), ReplayMarkerData::customData,
		ReplayMarkerData::new
	);

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private ReplayMarkerGroup group = ReplayMarkerGroup.DEFAULT;
		private Color color = null;
		private ResourceKey<Level> dimension = Level.OVERWORLD;
		private Optional<Vec3> position = Optional.empty();
		private String description = "";
		private Tag customData = null;

		public Builder group(ReplayMarkerGroup value) {
			group = value;
			return this;
		}

		public Builder color(Color value) {
			color = value;
			return this;
		}

		public Builder color(int value) {
			return color(Color.ofRGB(value));
		}

		public Builder dimension(ResourceKey<Level> value) {
			dimension = value;
			return this;
		}

		public Builder position(Optional<Vec3> value) {
			position = value;
			return this;
		}

		public Builder position(@Nullable Vec3 value) {
			return position(Optional.ofNullable(value));
		}

		public Builder position(Vec3i pos) {
			return position(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
		}

		public Builder description(String value) {
			description = value;
			return this;
		}

		public Builder customData(@Nullable Tag value) {
			customData = value;
			return this;
		}

		public Builder customData(String key, Tag value) {
			if (!(customData instanceof CompoundTag)) {
				customData = new CompoundTag();
			}

			((CompoundTag) customData).put(key, value);
			return this;
		}

		public Builder entityData(String key, Entity entity) {
			var tag = new CompoundTag();
			tag.putInt("id", entity.getId());
			tag.putLongArray("uuid", new long[]{entity.getUUID().getMostSignificantBits(), entity.getUUID().getLeastSignificantBits()});
			return customData(key, tag);
		}

		public Builder atEntity(Entity entity) {
			return entityData("entity", entity)
				.dimension(entity.level().dimension())
				.position(entity.position());
		}

		public ReplayMarkerData build() {
			return new ReplayMarkerData(
				group,
				Optional.ofNullable(color),
				dimension,
				position,
				description,
				Optional.ofNullable(customData)
			);
		}
	}

	public ReplayMarkerData(String description) {
		this(ReplayMarkerGroup.DEFAULT, Optional.empty(), Level.OVERWORLD, Optional.empty(), description, Optional.empty());
	}

	public Color getColor() {
		return color.orElse(group.defaultColor);
	}

	public String getDescription() {
		return description.isEmpty() ? group().getDisplayName() : description;
	}

	public boolean isVisibleInTimeline() {
		return group.visible;
	}

	public boolean isVisibleInWorld(ResourceKey<Level> dim) {
		return group.visible && group.renderInWorld && dimension == dim;
	}
}
