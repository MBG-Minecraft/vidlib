package dev.mrbeastgaming.mods.hub.file;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;

import java.util.Optional;
import java.util.UUID;

public record PossibleUser(
	Optional<Hex32> user,
	Optional<UUID> minecraftUuid,
	Optional<String> minecraftName
) {
	public static final PossibleUser NONE = new PossibleUser(Optional.empty(), Optional.empty(), Optional.empty());

	public static final Codec<PossibleUser> CODEC = RecordCodecBuilder.create(i -> i.group(
		Hex32.CODEC.optionalFieldOf("user").forGetter(PossibleUser::user),
		KLibCodecs.UUID.optionalFieldOf("minecraft_uuid").forGetter(PossibleUser::minecraftUuid),
		Codec.STRING.optionalFieldOf("minecraft_name").forGetter(PossibleUser::minecraftName)
	).apply(i, PossibleUser::new));

	public static PossibleUser of(UUID minecraftUuid) {
		return new PossibleUser(Optional.empty(), Optional.of(minecraftUuid), Optional.empty());
	}

	public static PossibleUser of(GameProfile profile) {
		return new PossibleUser(Optional.empty(), Optional.of(profile.getId()), Optional.of(profile.getName()));
	}
}
