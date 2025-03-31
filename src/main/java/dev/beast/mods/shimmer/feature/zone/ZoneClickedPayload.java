package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public record ZoneClickedPayload(ResourceLocation id, int index, ZoneShape shape, double distanceSq, Optional<Vec3> pos) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<ZoneClickedPayload> TYPE = ShimmerPacketType.internal("zone_clicked", CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID, ZoneClickedPayload::id,
		ByteBufCodecs.VAR_INT, ZoneClickedPayload::index,
		ZoneShape.STREAM_CODEC, ZoneClickedPayload::shape,
		ByteBufCodecs.DOUBLE, ZoneClickedPayload::distanceSq,
		ShimmerStreamCodecs.VEC_3.optional(), ZoneClickedPayload::pos,
		ZoneClickedPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		if (ShimmerTool.isHolding(ctx.player(), ZoneToolItem.class)) {
			var zone = ctx.level().shimmer$getActiveZones().get(id);

			if (zone != null) {
				NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(new ZoneClipResult(zone.zones.get(index), shape, distanceSq, pos.orElse(null), null), ctx.level(), ctx.player()));
			}
		}
	}
}
