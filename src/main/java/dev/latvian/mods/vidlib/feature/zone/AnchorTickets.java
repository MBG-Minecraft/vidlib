package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.session.ServerLevelSessionData;
import net.neoforged.neoforge.common.world.chunk.TicketController;

public interface AnchorTickets {
	TicketController TICKET_CONTROLLER = new TicketController(ID.vidlib("anchor"), (serverLevel, ticketHelper) -> ServerLevelSessionData.of(serverLevel).validateLoadedChunks(ticketHelper));
}
