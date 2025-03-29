package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.network.codec.ByteBufCodecs;

public record RedrawChunkSectionsPayload(LongList sections, boolean mainThread) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<RedrawChunkSectionsPayload> TYPE = ShimmerPacketType.internal("redraw_chunk_sections", CompositeStreamCodec.of(
		ShimmerStreamCodecs.LONG_LIST, RedrawChunkSectionsPayload::sections,
		ByteBufCodecs.BOOL, RedrawChunkSectionsPayload::mainThread,
		RedrawChunkSectionsPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().redrawSections(sections, mainThread);
	}
}
