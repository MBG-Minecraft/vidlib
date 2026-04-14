package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record HubProjectFullData(
	String gateway,
	HubProjectData project,
	List<HubParticipantData> participants
) {
	public static final Codec<HubProjectFullData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("gateway", "").forGetter(HubProjectFullData::gateway),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubProjectFullData::project),
		HubParticipantData.CODEC.listOf().optionalFieldOf("participants", List.of()).forGetter(HubProjectFullData::participants)
	).apply(instance, HubProjectFullData::new));
}
