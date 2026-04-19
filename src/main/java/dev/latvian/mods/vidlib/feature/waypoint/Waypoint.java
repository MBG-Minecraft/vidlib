package dev.latvian.mods.vidlib.feature.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.icon.TextureIcon;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.core.Position;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record Waypoint(
	String id,
	EntityFilter visible,
	ResourceKey<Level> dimension,
	KVector position,
	IconHolder icon,
	float alpha,
	double minDistance,
	double midDistance,
	double maxDistance,
	Component label,
	boolean centered,
	boolean showDistance,
	boolean ignoreHeight
) {
	public static final IconHolder DEFAULT_ICON = new TextureIcon(VidLibTextures.DEFAULT_MARKER).holder();
	public static final Component DEFAULT_LABEL = Component.empty();

	public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(Waypoint::id),
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY.instance()).forGetter(Waypoint::visible),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Waypoint::dimension),
		KVector.CODEC.fieldOf("position").forGetter(Waypoint::position),
		IconHolder.CODEC.optionalFieldOf("icon", DEFAULT_ICON).forGetter(Waypoint::icon),
		Codec.FLOAT.optionalFieldOf("alpha", 255F).forGetter(Waypoint::alpha),
		Codec.DOUBLE.optionalFieldOf("min_distance", 0D).forGetter(Waypoint::minDistance),
		Codec.DOUBLE.optionalFieldOf("mid_distance", 0D).forGetter(Waypoint::midDistance),
		Codec.DOUBLE.optionalFieldOf("max_distance", 0D).forGetter(Waypoint::maxDistance),
		ComponentSerialization.CODEC.optionalFieldOf("label", DEFAULT_LABEL).forGetter(Waypoint::label),
		Codec.BOOL.optionalFieldOf("centered", true).forGetter(Waypoint::centered),
		Codec.BOOL.optionalFieldOf("show_distance", true).forGetter(Waypoint::showDistance),
		Codec.BOOL.optionalFieldOf("ignore_height", true).forGetter(Waypoint::ignoreHeight)
	).apply(instance, Waypoint::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Waypoint> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, Waypoint::id,
		ByteBufCodecs.VAR_INT, Waypoint::getFlags,
		KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.ANY.instance()), Waypoint::visible,
		KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), Waypoint::dimension,
		KVector.STREAM_CODEC, Waypoint::position,
		KLibStreamCodecs.optional(IconHolder.STREAM_CODEC, DEFAULT_ICON), Waypoint::icon,
		ByteBufCodecs.FLOAT, Waypoint::alpha,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::minDistance,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::midDistance,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::maxDistance,
		KLibStreamCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC, DEFAULT_LABEL), Waypoint::label,
		Waypoint::new
	);

	public static final DataType<Waypoint> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Waypoint.class);
	public static final DataType<List<Waypoint>> LIST_DATA_TYPE = DATA_TYPE.listOf();

	public static class Builder {
		private String id = "";
		private EntityFilter visible = EntityFilter.ANY.instance();
		private ResourceKey<Level> dimension = Level.OVERWORLD;
		private KVector position = KVector.ZERO;
		private IconHolder icon = DEFAULT_ICON;
		private float alpha = 255F;
		private double minDistance = 0D;
		private double midDistance = 0D;
		private double maxDistance = 0D;
		private Component label = DEFAULT_LABEL;
		private boolean centered = true;
		private boolean showDistance = true;
		private boolean ignoreHeight = true;

		public Waypoint build() {
			return new Waypoint(
				id,
				visible,
				dimension,
				position,
				icon,
				alpha,
				minDistance,
				midDistance,
				maxDistance,
				label,
				centered,
				showDistance,
				ignoreHeight
			);
		}

		public Builder id(String value) {
			id = value;
			return this;
		}

		public Builder visible(EntityFilter value) {
			visible = value;
			return this;
		}

		public Builder dimension(ResourceKey<Level> value) {
			dimension = value;
			return this;
		}

		public Builder position(KVector value) {
			position = value;
			return this;
		}

		public Builder position(Position position) {
			return position(KVector.of(position));
		}

		public Builder icon(Icon value) {
			icon = value.holder();
			return this;
		}

		public Builder alpha(float value) {
			alpha = value;
			return this;
		}

		public Builder distance(double min, double mid, double max) {
			minDistance = min;
			midDistance = mid;
			maxDistance = max;
			return this;
		}

		public Builder label(Component value) {
			label = value;
			return this;
		}

		public Builder centered(boolean value) {
			centered = value;
			return this;
		}

		public Builder showDistance(boolean value) {
			showDistance = value;
			return this;
		}

		public Builder ignoreHeight(boolean value) {
			ignoreHeight = value;
			return this;
		}
	}

	private Waypoint(
		String id,
		int flags,
		EntityFilter filter,
		ResourceKey<Level> dimension,
		KVector position,
		IconHolder icon,
		float alpha,
		double minDistance,
		double midDistance,
		double maxDistance,
		Component label
	) {
		this(
			id,
			filter,
			dimension,
			position,
			icon,
			alpha,
			minDistance,
			midDistance,
			maxDistance,
			label,
			(flags & 2) != 0,
			(flags & 4) != 0,
			(flags & 8) != 0
		);
	}

	private int getFlags() {
		int flags = 0;
		flags |= centered ? 2 : 0;
		flags |= showDistance ? 4 : 0;
		flags |= ignoreHeight ? 8 : 0;
		return flags;
	}
}
