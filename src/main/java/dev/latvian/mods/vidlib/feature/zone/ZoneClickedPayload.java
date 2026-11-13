package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ZoneClickedPayload(Ref<Zone> zone, Vec3 pos, double distanceSq, Optional<Direction> direction, BlockPos blockPos) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<ZoneClickedPayload> TYPE = SimplePayloadType.vidlib("zone/clicked", CompositeStreamCodec.of(
		Zone.STREAM_CODEC, ZoneClickedPayload::zone,
		MCStreamCodecs.VEC3, ZoneClickedPayload::pos,
		ByteBufCodecs.DOUBLE, ZoneClickedPayload::distanceSq,
		ByteBufCodecs.optional(Direction.STREAM_CODEC), ZoneClickedPayload::direction,
		BlockPos.STREAM_CODEC, ZoneClickedPayload::blockPos,
		ZoneClickedPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var vl = ctx.vl();
		var hit = new ZoneHit(zone, vl.zones.getResolved(zone), pos, distanceSq, direction.orElse(null), blockPos);
		VLPlatformHelper.INSTANCE.playerClickedOnZone(ctx.player(), hit);
	}
}
