package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.util.MiscUtils;

public enum CaptureTaskType {
	SESSION_INFO(0, SessionInfoTask::new),
	COMPRESSED(1, CompressedTask::new),
	DISCONNECT(2, DisconnectTask::new),
	CORE_CONFIG_PACKET(3, CoreConfigPacketTask::new),
	CUSTOM_CONFIG_PACKET(4, CustomConfigPacketTask::new),
	CORE_GAME_PACKET(5, CoreGamePacketTask::new),
	CUSTOM_GAME_PACKET(6, CustomGamePacketTask::new),
	// CHUNK_PACKET(7, ChunkPacketTask::new), // WIP
	// VOID_CHUNK_PACKET(8, VoidChunkPacketTask::new), // WIP

	;

	public static final CaptureTaskType[] VALUES = values();
	public static final CaptureTaskType[] LOOKUP = MiscUtils.fastIndexedLookup(VALUES, t -> t.id, CaptureTaskType[]::new);

	public final int id;
	public final CaptureTaskFactory factory;

	CaptureTaskType(int id, CaptureTaskFactory factory) {
		this.id = id;
		this.factory = factory;
	}
}
