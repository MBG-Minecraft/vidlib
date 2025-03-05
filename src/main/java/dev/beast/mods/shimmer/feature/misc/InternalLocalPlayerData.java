package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.session.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class InternalLocalPlayerData extends PlayerData {
	public static final Codec<InternalLocalPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("render_zones", false).forGetter(d -> d.renderZones)
	).apply(instance, InternalLocalPlayerData::new));

	public static final StreamCodec<ByteBuf, InternalLocalPlayerData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		d -> d.renderZones,
		InternalLocalPlayerData::new
	);

	public boolean renderZones;

	InternalLocalPlayerData() {
		super(InternalPlayerData.LOCAL);
		this.renderZones = false;
	}

	private InternalLocalPlayerData(
		boolean renderZones
	) {
		super(InternalPlayerData.LOCAL);
		this.renderZones = renderZones;
	}
}
