package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.feature.capture.PlayerPacketCaptureSession;

import java.util.List;

public record WriteTasks(PlayerPacketCaptureSession session, List<CaptureTask> tasks) implements Runnable {
	@Override
	public void run() {
		session.write(tasks);
	}
}
