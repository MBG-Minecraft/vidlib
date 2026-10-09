package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubProjectFileLink;

import java.util.List;

public record HubProjectFileLinkRequest(
	List<HubProjectFileLink> links
) {
	public static final Codec<HubProjectFileLinkRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubProjectFileLink.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(HubProjectFileLinkRequest::links)
	).apply(i, HubProjectFileLinkRequest::new));
}