package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import net.minecraft.Util;

import java.util.UUID;

public record HubPossibleUser(
	Hex32 user,
	UUID minecraftUuid,
	String minecraftName
) {
	public static final HubPossibleUser NONE = new HubPossibleUser(Hex32.NONE, Util.NIL_UUID, "");

	public static final Codec<HubPossibleUser> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubDBObject.idCodec("user").forGetter(HubPossibleUser::user),
		KLibCodecs.UUID.optionalFieldOf("minecraft_uuid", Util.NIL_UUID).forGetter(HubPossibleUser::minecraftUuid),
		Codec.STRING.optionalFieldOf("minecraft_name", "").forGetter(HubPossibleUser::minecraftName)
	).apply(i, HubPossibleUser::new));

	public static HubPossibleUser of(Hex32 user) {
		return new HubPossibleUser(user, Util.NIL_UUID, "");
	}

	public static HubPossibleUser of(UUID minecraftUuid) {
		return new HubPossibleUser(Hex32.NONE, minecraftUuid, "");
	}

	public static HubPossibleUser of(GameProfile profile) {
		return new HubPossibleUser(Hex32.NONE, profile.getId(), profile.getName());
	}
}
