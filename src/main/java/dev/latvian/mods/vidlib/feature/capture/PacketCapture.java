package dev.latvian.mods.vidlib.feature.capture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.util.IOUtils;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.Timestamp;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.mutable.MutableInt;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PacketCapture {
	public static final UUID CAMERAMAN_UUID = UUID.fromString("069be141-3c1b-45c3-b3b1-60d3f9fcd236");

	public final MinecraftServer server;
	public final LevelData levelData;
	public final Timestamp sessionStart;
	public Timestamp sessionEnd;
	public final int sessionId;
	private final String toString;
	public final Path directory;
	public final Path tempDirectory;
	private final ThreadGroup threadGroup;
	public final ExecutorService executorService;
	public final Map<UUID, PlayerPacketCaptureSession> sessions;
	public final List<PlayerPacketCaptureSession> finishedSessions;
	private final Object2IntMap<ResourceLocation> identifierMap;
	private final Lock identifierMapLock;
	public final Map<UUID, MutableInt> sessionCounts;

	public PacketCapture(MinecraftServer server, int sessionId, Path directory) {
		this.server = server;
		this.levelData = server.getWorldData().overworldData();
		this.sessionStart = Timestamp.now(levelData.getGameTime());
		this.sessionEnd = Timestamp.NONE;
		this.sessionId = sessionId;
		var idString = "%08x".formatted(sessionId);
		this.toString = "Packet-Capture/" + idString;
		this.directory = directory;
		this.tempDirectory = directory.resolve(idString);
		this.threadGroup = server.getRunningThread().getThreadGroup();
		this.executorService = Executors.newSingleThreadExecutor(this::newThread);
		this.sessions = new ConcurrentHashMap<>();
		this.finishedSessions = new ArrayList<>();
		this.identifierMap = new Object2IntOpenHashMap<>();
		this.identifierMap.defaultReturnValue(0);
		this.identifierMapLock = new ReentrantLock();
		this.sessionCounts = new Object2ObjectOpenHashMap<>();
	}

	private Thread newThread(Runnable task) {
		var thread = new Thread(threadGroup, task, toString);
		thread.setDaemon(false);
		thread.setPriority(5);
		return thread;
	}

	public PlayerPacketCaptureSession getSession(UUID player) {
		return sessions.computeIfAbsent(player, uuid -> new PlayerPacketCaptureSession(this, uuid, sessionCounts.computeIfAbsent(uuid, u -> new MutableInt(0)).incrementAndGet()));
	}

	public void disconnect(UUID player) {
		var session = sessions.remove(player);

		if (session != null) {
			session.disconnect();
			finishedSessions.add(session);
		}
	}

	public void saveAll() {
		for (var session : sessions.values()) {
			session.save();
		}
	}

	private static void appendDec(StringBuilder builder, long num) {
		if (num < 0L) {
			builder.append('0');
		}

		builder.append(num);
	}

	public void finish() {
		sessionEnd = Timestamp.now(levelData.getGameTime());
		long seconds = sessionEnd.utc() - sessionStart.utc();

		VidLib.LOGGER.info("Saving captured packets... (%,d second session)".formatted(seconds));

		PlatformHelper.CURRENT.finishPacketCapture(this);

		for (var session : sessions.values()) {
			session.disconnect();
			finishedSessions.add(session);
		}

		var remainingTasks = executorService.shutdownNow();

		if (!remainingTasks.isEmpty()) {
			VidLib.LOGGER.info("Completing remaining tasks... (%,d)".formatted(remainingTasks.size()));
			remainingTasks.forEach(Runnable::run);
		}

		var filename = new StringBuilder();
		filename.append(Timestamp.deflateUTC(sessionStart.utc()));
		filename.append("-%08x-".formatted(sessionId));
		filename.append(seconds / 60L / 60L);
		filename.append("h-");
		appendDec(filename, (seconds / 60L) % 60L);
		filename.append("m-");
		appendDec(filename, seconds % 60L);
		filename.append("s.vlrec");

		var outputFile = directory.resolve(filename.toString());

		try (var fs = FileSystems.newFileSystem(URI.create("jar:" + outputFile.toUri()), Map.of("create", "true"))) {
			var metadata = new JsonObject();
			metadata.addProperty("platform", PlatformHelper.CURRENT.getPlatform());
			metadata.addProperty("id", "%08x".formatted(sessionId));
			metadata.add("start", Timestamp.CODEC.encodeStart(JsonOps.INSTANCE, sessionStart).getOrThrow());
			metadata.add("end", Timestamp.CODEC.encodeStart(JsonOps.INSTANCE, sessionEnd).getOrThrow());
			metadata.addProperty("length", seconds);

			var idMeta = new JsonObject();

			for (var entry : identifierMap.object2IntEntrySet()) {
				idMeta.addProperty(entry.getKey().toString(), entry.getIntValue());
			}

			metadata.add("identifier_map", idMeta);

			var sessionMeta = new JsonArray();

			for (var session : finishedSessions) {
				var nf = fs.getPath(session.fileName);
				VidLib.LOGGER.info("Writing %s...".formatted(session.fileName));
				Files.copy(session.writePath, nf, StandardCopyOption.REPLACE_EXISTING);

				var sm = new JsonObject();
				sm.addProperty("id", "%08x".formatted(session.info.id()));
				sm.addProperty("player", session.info.player().toString());
				sm.add("start", Timestamp.CODEC.encodeStart(JsonOps.INSTANCE, session.info.timestamp()).getOrThrow());
				sm.add("end", Timestamp.CODEC.encodeStart(JsonOps.INSTANCE, session.disconnected).getOrThrow());
				sm.addProperty("length", session.disconnected.utc() - session.info.timestamp().utc());
				sm.addProperty("file_name", session.fileName);
				sm.addProperty("file_size", Files.size(session.writePath));
				sessionMeta.add(sm);
			}

			metadata.add("sessions", sessionMeta);

			PlatformHelper.CURRENT.packetCaptureMetadata(this, metadata);

			try (var writer = Files.newBufferedWriter(fs.getPath("metadata.json"))) {
				JsonUtils.write(writer, metadata, false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		VidLib.LOGGER.info("Deleting temp directory...");

		try {
			IOUtils.deleteRecursively(tempDirectory);
			VidLib.LOGGER.info("Done! Captured packets @ %s (%,d KB)".formatted(filename, Files.size(outputFile) / 1000L));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return toString;
	}

	public int getIdentifier(ResourceLocation id) {
		identifierMapLock.lock();

		try {
			var i = identifierMap.getInt(id);

			if (i == 0) {
				i = identifierMap.size() + 1;
				identifierMap.put(id, i);
			}

			return i;
		} finally {
			identifierMapLock.unlock();
		}
	}
}
