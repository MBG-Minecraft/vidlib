package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;
import java.util.UUID;

@Deprecated
public record SyncPlayerTagsPayload(UUID player, List<String> tags) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncPlayerTagsPayload> TYPE = SimplePayloadType.vidlib("sync_player_tags", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SyncPlayerTagsPayload::player,
		KLibStreamCodecs.listOf(ByteBufCodecs.STRING_UTF8), SyncPlayerTagsPayload::tags,
		SyncPlayerTagsPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
	}
}
