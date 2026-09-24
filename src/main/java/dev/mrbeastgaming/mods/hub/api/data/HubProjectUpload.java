package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.util.Optional;

public record HubProjectUpload(
	Hex32 id,
	HubStoredFile file,
	String name,
	String path,
	HubFileType type,
	URI url,
	HubFileAttribute added,
	Optional<HubUser> assignedTo,
	boolean manual
) implements HubDBObject {
	public static final Codec<HubProjectUpload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubProjectUpload::id),
		HubStoredFile.CODEC.optionalFieldOf("file", HubStoredFile.UNKNOWN).forGetter(HubProjectUpload::file),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubProjectUpload::name),
		Codec.STRING.optionalFieldOf("path", "").forGetter(HubProjectUpload::path),
		HubFileType.CODEC.optionalFieldOf("type", HubFileType.UNKNOWN).forGetter(HubProjectUpload::type),
		HubAPI.URI_BASE_CODEC.fieldOf("url").forGetter(HubProjectUpload::url),
		HubFileAttribute.CODEC.optionalFieldOf("added", HubFileAttribute.DEFAULT).forGetter(HubProjectUpload::added),
		HubUser.CODEC.optionalFieldOf("assigned_to").forGetter(HubProjectUpload::assignedTo),
		Codec.BOOL.optionalFieldOf("manual", false).forGetter(HubProjectUpload::manual)
	).apply(instance, HubProjectUpload::new));
}
