package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubUserFlags(
	boolean bot,
	boolean admin,
	boolean staff,
	boolean anyStaff,
	boolean talent,
	boolean externalTalent,
	boolean internalTalent,
	boolean developer,
	boolean videoEditor
) {
	public static final MapCodec<HubUserFlags> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("is_bot", false).forGetter(HubUserFlags::bot),
		Codec.BOOL.optionalFieldOf("is_admin", false).forGetter(HubUserFlags::admin),
		Codec.BOOL.optionalFieldOf("is_staff", false).forGetter(HubUserFlags::staff),
		Codec.BOOL.optionalFieldOf("is_any_staff", false).forGetter(HubUserFlags::anyStaff),
		Codec.BOOL.optionalFieldOf("is_talent", false).forGetter(HubUserFlags::talent),
		Codec.BOOL.optionalFieldOf("is_external_talent", false).forGetter(HubUserFlags::externalTalent),
		Codec.BOOL.optionalFieldOf("is_internal_talent", false).forGetter(HubUserFlags::internalTalent),
		Codec.BOOL.optionalFieldOf("is_developer", false).forGetter(HubUserFlags::developer),
		Codec.BOOL.optionalFieldOf("is_video_editor", false).forGetter(HubUserFlags::videoEditor)
	).apply(instance, HubUserFlags::new));
}
