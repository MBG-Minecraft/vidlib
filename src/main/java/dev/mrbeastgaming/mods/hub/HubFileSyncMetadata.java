package dev.mrbeastgaming.mods.hub;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public record HubFileSyncMetadata(MD5 checksum, long size, Instant lastModified, boolean changed) {
	public static final Codec<HubFileSyncMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MD5.CODEC.fieldOf("md5").forGetter(HubFileSyncMetadata::checksum),
		Codec.LONG.fieldOf("size").forGetter(HubFileSyncMetadata::size),
		KLibCodecs.or(KLibCodecs.UINT64_INSTANT, KLibCodecs.INSTANT).fieldOf("last_modified").forGetter(HubFileSyncMetadata::lastModified),
		MapCodec.unit(false).forGetter(HubFileSyncMetadata::changed)
	).apply(instance, HubFileSyncMetadata::new));

	@Nullable
	public static HubFileSyncMetadata loadExisting(Path file) throws IOException {
		try {
			var attribute = IOUtils.getAttribute(file, "MBG-Hub-Sync-Metadata");

			if (!attribute.isEmpty()) {
				var attributeJson = JsonUtils.GSON.fromJson(attribute, JsonElement.class);
				return CODEC.parse(JsonOps.INSTANCE, attributeJson).getOrThrow();
			}
		} catch (Exception ignored) {
		}

		return null;
	}

	public static HubFileSyncMetadata load(Path file, @Nullable ProgressItem progressItem) throws NoSuchAlgorithmException, IOException {
		var existing = loadExisting(file);
		var size = Files.size(file);
		var lastModified = Instant.ofEpochSecond(Files.getLastModifiedTime(file).toInstant().getEpochSecond());

		if (existing == null || size != existing.size || lastModified.isAfter(existing.lastModified)) {
			var md = MessageDigest.getInstance("MD5");

			try (var channel = Files.newByteChannel(file)) {
				var buf = ByteBuffer.allocate(2048);
				int len;

				while ((len = channel.read(buf)) != -1) {
					buf.flip();
					md.update(buf);
					buf.clear();

					if (progressItem != null) {
						progressItem.addProgress(len);
					}
				}
			}

			return new HubFileSyncMetadata(
				MD5.fromBytes(md.digest()),
				size,
				lastModified,
				true
			);
		}

		return existing;
	}

	public static void save(Path file, HubFileSyncMetadata metadata) throws IOException {
		IOUtils.setAttribute(file, "MBG-Hub-Sync-Metadata", JsonUtils.string(CODEC.encodeStart(JsonOps.INSTANCE, metadata).getOrThrow()));
	}
}
