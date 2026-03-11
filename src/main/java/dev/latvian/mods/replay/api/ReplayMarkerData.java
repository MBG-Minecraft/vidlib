package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.color.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
	CompoundTag customData
) {
	public static final Color CHANGED_DIMENSION_COLOR = Color.ofRGB(0xAA00AA);
	private static final CompoundTag EMPTY_TAG = new CompoundTag(0);

	public static final Codec<ReplayMarkerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ReplayMarkerGroup.CODEC.optionalFieldOf("group", ReplayMarkerGroup.DEFAULT).forGetter(ReplayMarkerData::group),
		Color.SOLID_CODEC.optionalFieldOf("color").forGetter(ReplayMarkerData::color),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ReplayMarkerData::dimension),
		MCCodecs.VEC3.optionalFieldOf("position").forGetter(ReplayMarkerData::position),
		Codec.STRING.optionalFieldOf("description", "").forGetter(ReplayMarkerData::description),
		CompoundTag.CODEC.optionalFieldOf("custom_data", EMPTY_TAG).forGetter(ReplayMarkerData::customData)
	).apply(instance, ReplayMarkerData::new));

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private ReplayMarkerGroup group = ReplayMarkerGroup.DEFAULT;
		private Color color = null;
		private ResourceKey<Level> dimension = Level.OVERWORLD;
		private Optional<Vec3> position = Optional.empty();
		private String description = "";
		private CompoundTag customData = EMPTY_TAG;

		public Builder group(ReplayMarkerGroup value) {
			group = value;
			return this;
		}

		public Builder color(Color value) {
			color = value;
			return this;
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

		public Builder description(String value) {
			description = value;
			return this;
		}

		public Builder customData(CompoundTag value) {
			customData = value;
			return this;
		}

		public ReplayMarkerData build() {
			return new ReplayMarkerData(
				group,
				Optional.ofNullable(color),
				dimension,
				position,
				description,
				customData
			);
		}
	}

	public ReplayMarkerData(String description) {
		this(ReplayMarkerGroup.DEFAULT, Optional.empty(), Level.OVERWORLD, Optional.empty(), description, EMPTY_TAG);
	}

	public Color getColor() {
		return color.orElse(group.defaultColor);
	}
}
