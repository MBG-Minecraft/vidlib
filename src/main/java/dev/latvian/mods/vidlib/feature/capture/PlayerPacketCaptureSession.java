package dev.latvian.mods.vidlib.feature.capture;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.capture.task.CaptureTask;
import dev.latvian.mods.vidlib.feature.capture.task.CompressedTask;
import dev.latvian.mods.vidlib.feature.capture.task.CoreConfigPacketTask;
import dev.latvian.mods.vidlib.feature.capture.task.CoreGamePacketTask;
import dev.latvian.mods.vidlib.feature.capture.task.CustomConfigPacketTask;
import dev.latvian.mods.vidlib.feature.capture.task.CustomGamePacketTask;
import dev.latvian.mods.vidlib.feature.capture.task.DisconnectTask;
import dev.latvian.mods.vidlib.feature.capture.task.SessionInfoTask;
import dev.latvian.mods.vidlib.feature.capture.task.WriteTasks;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.util.IOUtils;
import dev.latvian.mods.vidlib.util.Timestamp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.GameProtocols;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerPacketCaptureSession {
	private final PacketCapture packetCapture;
	public final SessionInfoTask info;
	public PlayerPacketCaptureStatus status;
	public final String fileName;
	public final Path writePath;
	public final List<CaptureTask> taskQueue;
	private final Lock lock;
	private final FriendlyByteBuf configBuf;
	private final RegistryFriendlyByteBuf gameBuf;
	private final StreamCodec<ByteBuf, Packet<? super ClientConfigurationPacketListener>> configPacketFactory;
	private final StreamCodec<ByteBuf, Packet<? super ClientGamePacketListener>> gamePacketFactory;
	public Timestamp disconnected;

	public PlayerPacketCaptureSession(PacketCapture packetCapture, UUID player, int sessionId) {
		this.packetCapture = packetCapture;
		this.info = new SessionInfoTask(sessionId, player, Timestamp.now(packetCapture.levelData.getGameTime()));
		var idString = "%08x".formatted(sessionId);
		this.status = PlayerPacketCaptureStatus.UNINITIALIZED;
		this.fileName = Timestamp.deflateUTC(info.timestamp().utc()) + "-" + player + "-" + idString + ".vlrecplayer";
		this.writePath = packetCapture.tempDirectory.resolve(fileName);
		this.taskQueue = new ArrayList<>();
		this.taskQueue.add(info);
		this.lock = new ReentrantLock();
		this.configBuf = new FriendlyByteBuf(Unpooled.buffer());
		this.gameBuf = PlatformHelper.CURRENT.createBuffer(Unpooled.buffer(), packetCapture.server.registryAccess());
		this.configPacketFactory = ConfigurationProtocols.CLIENTBOUND.codec();
		this.gamePacketFactory = GameProtocols.CLIENTBOUND_TEMPLATE.bind(PlatformHelper.CURRENT.createDecorator(packetCapture.server.registryAccess())).codec();
		this.disconnected = Timestamp.NONE;
	}

	public void capture(Packet<?> packet, boolean config) {
		var time = Timestamp.now(packetCapture.levelData.getGameTime());
		lock.lock();

		try {
			if (status == PlayerPacketCaptureStatus.UNINITIALIZED) {
				if (config) {
					status = PlayerPacketCaptureStatus.CONFIG;
				} else {
					throw new IllegalStateException("Received game packets before any config packets!");
				}
			} else if (status == PlayerPacketCaptureStatus.CONFIG) {
				if (!config) {
					status = PlayerPacketCaptureStatus.GAME;
				}
			} else if (status == PlayerPacketCaptureStatus.GAME) {
				if (config) {
					throw new IllegalStateException("Received config packets after game packets!");
				}
			} else if (status == PlayerPacketCaptureStatus.FINISHED) {
				throw new IllegalStateException("Received game packets after disconnect packets!");
			}

			capture0(time, packet, config);
		} finally {
			lock.unlock();
		}
	}

	private void capture0(Timestamp time, Packet<?> packet, boolean config) {
		if (packet instanceof ClientboundBundlePacket bundle) {
			for (var p : bundle.subPackets()) {
				capture0(time, p, config);
			}
		} else if (packet instanceof ClientboundCustomPayloadPacket custom) {
			var type = custom.payload().type().id();

			if (config) {
				ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.encode(configBuf, custom);
				var bytes = new byte[configBuf.readableBytes()];
				configBuf.getBytes(configBuf.readerIndex(), bytes);
				configBuf.resetReaderIndex();
				configBuf.resetWriterIndex();
				taskQueue.add(new CustomConfigPacketTask(type, bytes));
			} else {
				ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.encode(gameBuf, custom);
				var bytes = new byte[gameBuf.readableBytes()];
				gameBuf.getBytes(gameBuf.readerIndex(), bytes);
				gameBuf.resetReaderIndex();
				gameBuf.resetWriterIndex();

				if (bytes.length >= 1024) {
					taskQueue.add(new CompressedTask(new CustomGamePacketTask(time, type, bytes)));
				} else {
					taskQueue.add(new CustomGamePacketTask(time, type, bytes));
				}
			}
		} else {
			var type = packet.type().id();

			if (config) {
				configPacketFactory.encode(configBuf, (Packet) packet);
				var bytes = new byte[configBuf.readableBytes()];
				configBuf.getBytes(configBuf.readerIndex(), bytes);
				configBuf.resetReaderIndex();
				configBuf.resetWriterIndex();
				taskQueue.add(new CoreConfigPacketTask(type, bytes));
			} else {
				gamePacketFactory.encode(gameBuf, (Packet) packet);
				var bytes = new byte[gameBuf.readableBytes()];
				gameBuf.getBytes(gameBuf.readerIndex(), bytes);
				gameBuf.resetReaderIndex();
				gameBuf.resetWriterIndex();

				if (bytes.length >= 1024) {
					taskQueue.add(new CompressedTask(new CoreGamePacketTask(time, type, bytes)));
				} else {
					taskQueue.add(new CoreGamePacketTask(time, type, bytes));
				}
			}
		}
	}

	public void disconnect() {
		disconnected = Timestamp.now(packetCapture.levelData.getGameTime());
		status = PlayerPacketCaptureStatus.FINISHED;
		taskQueue.add(new DisconnectTask(disconnected));
		save();
		configBuf.release();
		gameBuf.release();
	}

	public void save() {
		List<CaptureTask> tasks;
		lock.lock();

		try {
			tasks = List.copyOf(taskQueue);
			taskQueue.clear();
		} finally {
			lock.unlock();
		}

		if (!tasks.isEmpty()) {
			packetCapture.executorService.execute(new WriteTasks(this, tasks));
		}
	}

	public void write(List<CaptureTask> tasks) {
		if (Files.notExists(writePath)) {
			var parent = writePath.getParent();

			if (Files.notExists(parent)) {
				try {
					Files.createDirectories(parent);
				} catch (Exception ex) {
					throw new RuntimeException("Failed to create packet capture directory", ex);
				}
			}

			try {
				Files.createFile(writePath);

				try (var out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(writePath)))) {
					out.writeByte(0); // Binary marker
					out.writeByte(1); // Version

					for (var task : tasks) {
						task.writeFully(packetCapture, out);
					}
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to create the file of %s capture session %08X".formatted(info.player(), info.id()), ex);
			}

			return;
		}

		try (var out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(writePath, IOUtils.APPEND_OPEN_OPTIONS.toArray(new OpenOption[0]))))) {
			for (var task : tasks) {
				task.writeFully(packetCapture, out);
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to write packets of %s capture session %08X".formatted(info.player(), info.id()), ex);
		}
	}

	@Override
	public String toString() {
		return fileName;
	}
}
