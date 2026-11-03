package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public record ZoneClickedPayload(Ref<ZoneContainer> zone, int index, Vec3 pos, double distanceSq, Optional<Direction> direction) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<ZoneClickedPayload> TYPE = SimplePayloadType.vidlib("zone/clicked", CompositeStreamCodec.of(
		ZoneContainer.STREAM_CODEC, ZoneClickedPayload::zone,
		ByteBufCodecs.VAR_INT, ZoneClickedPayload::index,
		MCStreamCodecs.VEC3, ZoneClickedPayload::pos,
		ByteBufCodecs.DOUBLE, ZoneClickedPayload::distanceSq,
		ByteBufCodecs.optional(Direction.STREAM_CODEC), ZoneClickedPayload::direction,
		ZoneClickedPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (VidLibTool.isHolding(ctx.player(), ZoneTool.INSTANCE)) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(new ZoneHit(zone.value().zones.get(index), pos, distanceSq, direction.orElse(null)), ctx.level(), ctx.player()));
		}
	}
}
