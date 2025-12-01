package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;

public record PlayerProfile(GameProfile profile, long lastUpdate, Optional<String> skinUrl, boolean slimModel) {
	public static final GameProfile EMPTY_GAME_PROFILE = new GameProfile(Util.NIL_UUID, "");
	public static final PlayerProfile ERROR = new PlayerProfile(EMPTY_GAME_PROFILE, 0L, Optional.empty(), false);

	public static final Codec<PlayerProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ExtraCodecs.GAME_PROFILE.fieldOf("profile").forGetter(PlayerProfile::profile),
		Codec.LONG.fieldOf("last_update").forGetter(PlayerProfile::lastUpdate),
		Codec.STRING.optionalFieldOf("skin_url").forGetter(PlayerProfile::skinUrl),
		Codec.BOOL.optionalFieldOf("slim_model", false).forGetter(PlayerProfile::slimModel)
	).apply(instance, PlayerProfile::new));

	public static final Codec<List<PlayerProfile>> LIST_CODEC = CODEC.listOf();

	public boolean isError() {
		return lastUpdate == 0L;
	}
}
