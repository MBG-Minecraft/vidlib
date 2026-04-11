package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubProjectFullData(
	String gateway,
	HubProjectData project
) {
	public static final Codec<HubProjectFullData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("gateway", "").forGetter(HubProjectFullData::gateway),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubProjectFullData::project)

	).apply(instance, HubProjectFullData::new));
}
