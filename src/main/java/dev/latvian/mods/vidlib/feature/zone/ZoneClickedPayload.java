package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public record ZoneClickedPayload(Ref<ZoneContainer> zone, int index, double distanceSq, Optional<Vec3> pos) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<ZoneClickedPayload> TYPE = VidLibPacketType.internal("zone/clicked", CompositeStreamCodec.of(
		ZoneContainer.STREAM_CODEC, ZoneClickedPayload::zone,
		ByteBufCodecs.VAR_INT, ZoneClickedPayload::index,
		ByteBufCodecs.DOUBLE, ZoneClickedPayload::distanceSq,
		ByteBufCodecs.optional(MCStreamCodecs.VEC3), ZoneClickedPayload::pos,
		ZoneClickedPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (VidLibTool.isHolding(ctx.player(), ZoneTool.INSTANCE)) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(new ZoneClipResult(zone.value().zones.get(index), distanceSq, pos.orElse(null), null), ctx.level(), ctx.player()));
		}
	}
}
