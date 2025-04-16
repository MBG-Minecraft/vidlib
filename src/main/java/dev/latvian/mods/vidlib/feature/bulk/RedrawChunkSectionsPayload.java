package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.network.codec.ByteBufCodecs;

public record RedrawChunkSectionsPayload(LongList sections, boolean mainThread) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RedrawChunkSectionsPayload> TYPE = VidLibPacketType.internal("redraw_chunk_sections", CompositeStreamCodec.of(
		VLStreamCodecs.LONG_LIST, RedrawChunkSectionsPayload::sections,
		ByteBufCodecs.BOOL, RedrawChunkSectionsPayload::mainThread,
		RedrawChunkSectionsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().redrawSections(sections, mainThread);
	}
}
