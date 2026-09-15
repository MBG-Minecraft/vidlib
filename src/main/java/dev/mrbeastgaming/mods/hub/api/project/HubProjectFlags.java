package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubProjectFlags(
	boolean visible,
	boolean archived,
	boolean shortForm,
	boolean basePack,
	boolean drive
) {
	public static final HubProjectFlags EMPTY = new HubProjectFlags(
		false,
		false,
		false,
		false,
		false
	);

	public static final MapCodec<HubProjectFlags> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("visible", false).forGetter(HubProjectFlags::visible),
		Codec.BOOL.optionalFieldOf("archived", false).forGetter(HubProjectFlags::archived),
		Codec.BOOL.optionalFieldOf("short_form", false).forGetter(HubProjectFlags::shortForm),
		Codec.BOOL.optionalFieldOf("base_pack", false).forGetter(HubProjectFlags::basePack),
		Codec.BOOL.optionalFieldOf("drive", false).forGetter(HubProjectFlags::drive)
	).apply(instance, HubProjectFlags::new));

	public static final Codec<HubProjectFlags> CODEC = MAP_CODEC.codec();
}
