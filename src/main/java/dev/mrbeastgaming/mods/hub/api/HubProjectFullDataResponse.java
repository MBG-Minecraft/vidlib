package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubParticipant;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;

import java.util.List;

public record HubProjectFullDataResponse(
	HubResponseContext ctx,
	String gateway,
	HubProject project,
	List<HubParticipant> participants
) {
	public static final Codec<HubProjectFullDataResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubResponseContext.MAP_CODEC.forGetter(HubProjectFullDataResponse::ctx),
		Codec.STRING.optionalFieldOf("gateway", "").forGetter(HubProjectFullDataResponse::gateway),
		HubProject.DIRECT_CODEC.fieldOf("project").forGetter(HubProjectFullDataResponse::project),
		HubParticipant.CODEC.listOf().optionalFieldOf("participants", List.of()).forGetter(HubProjectFullDataResponse::participants)
	).apply(instance, HubProjectFullDataResponse::new));
}
