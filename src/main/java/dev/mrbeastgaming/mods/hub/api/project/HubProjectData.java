package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;
import dev.mrbeastgaming.mods.hub.api.HubDataMap;
import dev.mrbeastgaming.mods.hub.api.HubGameData;

import java.util.List;

public record HubProjectData(
	Hex32 id,
	String name,
	String description,
	String productionCode,
	HubGameData game,
	UInt64 discordGuild,
	boolean visible,
	List<HubTeamData> teams,
	HubDataMap customData,
	boolean archived
) {
	public static final Codec<HubProjectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubProjectData::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubProjectData::name),
		Codec.STRING.optionalFieldOf("description", "").forGetter(HubProjectData::description),
		Codec.STRING.optionalFieldOf("production_code", "").forGetter(HubProjectData::productionCode),
		HubGameData.CODEC.fieldOf("game").forGetter(HubProjectData::game),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubProjectData::discordGuild),
		Codec.BOOL.optionalFieldOf("visible", false).forGetter(HubProjectData::visible),
		HubTeamData.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(HubProjectData::teams),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubProjectData::customData),
		Codec.BOOL.optionalFieldOf("archived", false).forGetter(HubProjectData::archived)
	).apply(instance, HubProjectData::new));

	public static HubProjectData PACK = null;

	@Override
	public String toString() {
		return name + "#" + id;
	}
}
