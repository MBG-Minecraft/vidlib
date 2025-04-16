package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;

public record SectionModifiedPayload(boolean undoable, SectionPos section, BulkLevelModification modification) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SectionModifiedPayload> TYPE = VidLibPacketType.internal("section_modified", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, SectionModifiedPayload::undoable,
		VLStreamCodecs.SECTION_POS, SectionModifiedPayload::section,
		BulkLevelModification.REGISTRY.valueStreamCodec(), SectionModifiedPayload::modification,
		SectionModifiedPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().bulkModify(undoable, modification);
	}
}
