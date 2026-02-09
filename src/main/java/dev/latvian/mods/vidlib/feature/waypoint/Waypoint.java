package dev.latvian.mods.vidlib.feature.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

public record Waypoint(
	String id,
	boolean enabled,
	EntityFilter filter,
	ResourceKey<Level> dimension,
	KVector position,
	ResourceLocation icon,
	Color tint,
	double minDistance,
	double midDistance,
	double maxDistance,
	Component label,
	boolean centered,
	boolean showDistance
) {
	public static final Component DEFAULT_LABEL = Component.empty();

	public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(Waypoint::id),
		Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Waypoint::enabled),
		EntityFilter.CODEC.optionalFieldOf("filter", EntityFilter.ANY.instance()).forGetter(Waypoint::filter),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Waypoint::dimension),
		KVector.CODEC.fieldOf("position").forGetter(Waypoint::position),
		ID.CODEC.optionalFieldOf("icon", VidLibTextures.DEFAULT_MARKER).forGetter(Waypoint::icon),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(Waypoint::tint),
		Codec.DOUBLE.optionalFieldOf("min_distance", 0D).forGetter(Waypoint::minDistance),
		Codec.DOUBLE.optionalFieldOf("mid_distance", 0D).forGetter(Waypoint::midDistance),
		Codec.DOUBLE.optionalFieldOf("max_distance", 0D).forGetter(Waypoint::maxDistance),
		ComponentSerialization.CODEC.optionalFieldOf("label", DEFAULT_LABEL).forGetter(Waypoint::label),
		Codec.BOOL.optionalFieldOf("centered", true).forGetter(Waypoint::centered),
		Codec.BOOL.optionalFieldOf("show_distance", true).forGetter(Waypoint::showDistance)
	).apply(instance, Waypoint::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Waypoint> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, Waypoint::id,
		ByteBufCodecs.VAR_INT, Waypoint::getFlags,
		KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.ANY.instance()), Waypoint::filter,
		KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), Waypoint::dimension,
		KVector.STREAM_CODEC, Waypoint::position,
		KLibStreamCodecs.optional(ID.STREAM_CODEC, VidLibTextures.DEFAULT_MARKER), Waypoint::icon,
		Color.STREAM_CODEC, Waypoint::tint,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::minDistance,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::midDistance,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE32, 0D), Waypoint::maxDistance,
		KLibStreamCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC, DEFAULT_LABEL), Waypoint::label,
		Waypoint::new
	);

	public static final DataType<Waypoint> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Waypoint.class);
	public static final DataType<List<Waypoint>> LIST_DATA_TYPE = DATA_TYPE.listOf();

	public static class Builder {
		public String id = "";
		public boolean enabled = true;
		public EntityFilter filter = EntityFilter.ANY.instance();
		public ResourceKey<Level> dimension = Level.OVERWORLD;
		public KVector position = KVector.ZERO;
		public ResourceLocation icon = VidLibTextures.DEFAULT_MARKER;
		public Color tint = Color.WHITE;
		public double minDistance = 0D;
		public double midDistance = 0D;
		public double maxDistance = 0D;
		public Component label = DEFAULT_LABEL;
		public boolean centered = true;
		public boolean showDistance = true;

		public Waypoint build() {
			return new Waypoint(
				id,
				enabled,
				filter,
				dimension,
				position,
				icon,
				tint,
				minDistance,
				midDistance,
				maxDistance,
				label,
				centered,
				showDistance
			);
		}
	}

	private Waypoint(
		String id,
		int flags,
		EntityFilter filter,
		ResourceKey<Level> dimension,
		KVector position,
		ResourceLocation icon,
		Color tint,
		double minDistance,
		double midDistance,
		double maxDistance,
		Component label
	) {
		this(
			id,
			(flags & 1) != 0,
			filter,
			dimension,
			position,
			icon,
			tint,
			minDistance,
			midDistance,
			maxDistance,
			label,
			(flags & 2) != 0,
			(flags & 4) != 0
		);
	}

	private int getFlags() {
		int flags = 0;
		flags |= enabled ? 1 : 0;
		flags |= centered ? 2 : 0;
		flags |= showDistance ? 4 : 0;
		return flags;
	}
}
