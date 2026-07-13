package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.Optional;

public record HubLogRequest(
	Optional<Instant> time,
	int type,
	String content,
	int source,
	String dimension,
	Optional<Vec3> coordinates,
	long gameTick,
	Optional<JsonElement> customData
) {
	public static final Codec<HubLogRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.INSTANT.optionalFieldOf("time").forGetter(HubLogRequest::time),
		Codec.INT.optionalFieldOf("type", 0).forGetter(HubLogRequest::type),
		Codec.STRING.optionalFieldOf("content", "").forGetter(HubLogRequest::content),
		Codec.INT.optionalFieldOf("source", 0).forGetter(HubLogRequest::source),
		Codec.STRING.optionalFieldOf("dimension", "").forGetter(HubLogRequest::dimension),
		Vec3.CODEC.optionalFieldOf("coordinates").forGetter(HubLogRequest::coordinates),
		Codec.LONG.optionalFieldOf("game_tick", 0L).forGetter(HubLogRequest::gameTick),
		ExtraCodecs.JSON.optionalFieldOf("custom_data").forGetter(HubLogRequest::customData)
	).apply(instance, HubLogRequest::new));

	public HubLogRequest(Optional<Instant> time, int type, String content) {
		this(
			time,
			type,
			content,
			0,
			"",
			Optional.empty(),
			0L,
			Optional.empty()
		);
	}

	public HubLogRequest(Optional<Instant> time, int type, String content, Player player) {
		this(
			time,
			type,
			content,
			0,
			(PlatformHelper.CURRENT.isReplayLevel(player.level()) ? "replay:" : "") + player.level().dimension().location().toString(),
			Optional.of(player.position()),
			player.level().getGameTime(),
			Optional.empty()
		);
	}
}
