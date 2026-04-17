package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

import java.util.List;

public record HubProjectData(
	Hex32 id,
	String internalName,
	String publicName,
	String description,
	HubGameData game,
	UInt64 discordGuild,
	boolean visible,
	List<HubTeamData> teams,
	HubDataMap customData
) {
	public static final Codec<HubProjectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubProjectData::id),
		Codec.STRING.optionalFieldOf("internal_name", "").forGetter(HubProjectData::internalName),
		Codec.STRING.optionalFieldOf("public_name", "").forGetter(HubProjectData::publicName),
		Codec.STRING.optionalFieldOf("description", "").forGetter(HubProjectData::description),
		HubGameData.CODEC.fieldOf("game").forGetter(HubProjectData::game),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubProjectData::discordGuild),
		Codec.BOOL.optionalFieldOf("visible", false).forGetter(HubProjectData::visible),
		HubTeamData.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(HubProjectData::teams),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubProjectData::customData)
	).apply(instance, HubProjectData::new));

	public static HubProjectData PACK = null;

	public String displayName() {
		return publicName.isEmpty() ? internalName : publicName;
	}

	@Override
	public String toString() {
		return displayName() + "#" + id;
	}
}
