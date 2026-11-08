package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.ServerLevelSessionData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.List;

public record Anchor(List<AAIBB> volumes) {
	public static final Anchor NONE = new Anchor(List.of());

	public static Anchor create(List<AAIBB> volumes) {
		return volumes.isEmpty() ? NONE : new Anchor(volumes);
	}

	public static Anchor create(BlockPos start, BlockPos end) {
		return create(List.of(new AAIBB(start, end)));
	}

	public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		AAIBB.CODEC.listOf().optionalFieldOf("volumes", List.of()).forGetter(Anchor::volumes)
	).apply(instance, Anchor::create));

	public static final StreamCodec<RegistryFriendlyByteBuf, Anchor> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(AAIBB.STREAM_CODEC), Anchor::volumes,
		Anchor::create
	);

	public static final DataType<Anchor> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);
	public static final TicketController TICKET_CONTROLLER = new TicketController(ID.vidlib("anchor"), (serverLevel, ticketHelper) -> ServerLevelSessionData.of(serverLevel).validateLoadedChunks(ticketHelper));

	public static final TicketController BLOCK_TICKET_CONTROLLER = new TicketController(ID.vidlib("anchor_blocks"), (level, ticketHelper) -> {
		for (var pos : ticketHelper.getBlockTickets().keySet()) {
			if (!CommonGameEngine.INSTANCE.isBlockAnchor(level, pos, level.getBlockState(pos))) {
				ticketHelper.removeTicket(pos, ChunkPos.pack(pos), false);
			}
		}
	});

	public static void setAnchorBlockActive(ServerLevel level, BlockPos pos, boolean active) {
		BLOCK_TICKET_CONTROLLER.forceChunk(level, pos, pos.getX() >> 4, pos.getZ() >> 4, active, false);
	}
}
