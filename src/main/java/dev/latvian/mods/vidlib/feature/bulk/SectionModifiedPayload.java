package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;

public record SectionModifiedPayload(boolean undoable, SectionPos section, Ref<BulkLevelModification> modification) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SectionModifiedPayload> TYPE = SimplePayloadType.vidlib("section_modified", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, SectionModifiedPayload::undoable,
		MCStreamCodecs.SECTION_POS, SectionModifiedPayload::section,
		BulkLevelModification.REGISTRY.streamCodec(), SectionModifiedPayload::modification,
		SectionModifiedPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().bulkModify(undoable, modification);
	}
}
