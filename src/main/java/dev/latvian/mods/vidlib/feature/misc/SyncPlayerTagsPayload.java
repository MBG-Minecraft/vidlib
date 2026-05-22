package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;
import java.util.UUID;

@Deprecated
public record SyncPlayerTagsPayload(UUID player, List<String> tags) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncPlayerTagsPayload> TYPE = VidLibPacketType.internal("sync_player_tags", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SyncPlayerTagsPayload::player,
		KLibStreamCodecs.listOf(ByteBufCodecs.STRING_UTF8), SyncPlayerTagsPayload::tags,
		SyncPlayerTagsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
	}
}
