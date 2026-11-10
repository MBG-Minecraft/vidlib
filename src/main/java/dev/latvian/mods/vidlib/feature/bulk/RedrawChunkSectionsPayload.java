package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.network.codec.ByteBufCodecs;

public record RedrawChunkSectionsPayload(LongList sections, boolean mainThread) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RedrawChunkSectionsPayload> TYPE = SimplePayloadType.vidlib("redraw_chunk_sections", CompositeStreamCodec.of(
		CollectionStreamCodecs.LONG_LIST, RedrawChunkSectionsPayload::sections,
		ByteBufCodecs.BOOL, RedrawChunkSectionsPayload::mainThread,
		RedrawChunkSectionsPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().redrawSections(sections, mainThread);
	}
}
