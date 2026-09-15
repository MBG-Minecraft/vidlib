package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;
import dev.mrbeastgaming.mods.hub.api.HubDataMap;

import java.util.List;

public record HubProjectData(
	HubProjectDisplayData display,
	UInt64 discordGuild,
	List<HubTeamData> teams,
	HubDataMap customData
) {
	public static final Codec<HubProjectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubProjectDisplayData.MAP_CODEC.forGetter(HubProjectData::display),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubProjectData::discordGuild),
		HubTeamData.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(HubProjectData::teams),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubProjectData::customData)
	).apply(instance, HubProjectData::new));

	public static HubProjectData PACK = null;

	public Hex32 id() {
		return display.id();
	}

	@Override
	public String toString() {
		return display.toString();
	}
}
