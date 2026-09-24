package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import net.minecraft.Util;

import java.util.Optional;
import java.util.UUID;

public record HubPossibleUser(
	Optional<HubUser> user,
	UUID minecraftUuid,
	String minecraftName
) {
	public static final HubPossibleUser NONE = new HubPossibleUser(Optional.empty(), Util.NIL_UUID, "");

	public static final Codec<HubPossibleUser> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubPossibleUser::user),
		KLibCodecs.UUID.optionalFieldOf("minecraft_uuid", Util.NIL_UUID).forGetter(HubPossibleUser::minecraftUuid),
		Codec.STRING.optionalFieldOf("minecraft_name", "").forGetter(HubPossibleUser::minecraftName)
	).apply(i, HubPossibleUser::new));

	public static HubPossibleUser of(HubUser user) {
		return new HubPossibleUser(Optional.ofNullable(user), Util.NIL_UUID, "");
	}

	public static HubPossibleUser of(UUID minecraftUuid) {
		return new HubPossibleUser(Optional.empty(), minecraftUuid, "");
	}

	public static HubPossibleUser of(GameProfile profile) {
		return new HubPossibleUser(Optional.empty(), profile.getId(), profile.getName());
	}
}
