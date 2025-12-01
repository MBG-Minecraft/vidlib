package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class Waypoint {
	public static final Component DEFAULT_LABEL = Component.empty();

	public static final StreamCodec<RegistryFriendlyByteBuf, Waypoint> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, m -> m.id,
		ByteBufCodecs.VAR_INT, Waypoint::getFlags,
		KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD), m -> m.dimension,
		KVector.STREAM_CODEC, m -> m.position,
		KLibStreamCodecs.optional(ID.STREAM_CODEC, VidLibTextures.DEFAULT_MARKER), m -> m.icon,
		Color.STREAM_CODEC, m -> m.tint,
		KLibStreamCodecs.optional(KLibStreamCodecs.DOUBLE_AS_FLOAT, 0D), m -> m.maxDistance,
		KLibStreamCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC, DEFAULT_LABEL), m -> m.label,
		Waypoint::new
	);

	public String id = "";
	public boolean enabled = true;
	public ResourceKey<Level> dimension = Level.OVERWORLD;
	public KVector position = KVector.ZERO;
	public ResourceLocation icon = VidLibTextures.DEFAULT_MARKER;
	public Color tint = Color.WHITE;
	public double maxDistance = 0D;
	public Component label = DEFAULT_LABEL;
	public boolean centered = true;
	public boolean showDistance = true;

	public Waypoint() {
	}

	private Waypoint(String id, int flags, ResourceKey<Level> dimension, KVector position, ResourceLocation icon, Color tint, double maxDistance, Component label) {
		this.id = id;
		this.enabled = (flags & 1) != 0;
		this.dimension = dimension;
		this.position = position;
		this.icon = icon;
		this.tint = tint;
		this.maxDistance = maxDistance;
		this.label = label;
		this.centered = (flags & 2) != 0;
		this.showDistance = (flags & 4) != 0;
	}

	private int getFlags() {
		int flags = 0;
		flags |= enabled ? 1 : 0;
		flags |= centered ? 2 : 0;
		flags |= showDistance ? 4 : 0;
		return flags;
	}
}
