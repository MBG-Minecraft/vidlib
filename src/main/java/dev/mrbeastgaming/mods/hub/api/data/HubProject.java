package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record HubProject(
	Hex32 id,
	HubProjectFlags flags,
	String name,
	Optional<URI> iconUrl,
	Optional<URI> smallIconUrl,
	String description,
	String productionCode,
	HubGame game,
	UInt64 discordGuild,
	List<Hex32> teams,
	HubDataMap customData
) implements HubDBObject {
	public static final Codec<HubProject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubProject::id),
		HubProjectFlags.CODEC.optionalFieldOf("flags", HubProjectFlags.NONE).forGetter(HubProject::flags),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubProject::name),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(HubProject::iconUrl),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("small_icon_url").forGetter(HubProject::smallIconUrl),
		Codec.STRING.optionalFieldOf("description", "").forGetter(HubProject::description),
		Codec.STRING.optionalFieldOf("production_code", "").forGetter(HubProject::productionCode),
		HubGame.CODEC.fieldOf("game").forGetter(HubProject::game),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubProject::discordGuild),
		HubDBObject.idListCodec("teams").forGetter(HubProject::teams),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubProject::customData)
	).apply(instance, HubProject::new));

	@Override
	public String toString() {
		return name + "#" + id;
	}
}
