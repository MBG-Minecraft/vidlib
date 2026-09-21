package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.SHA256;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record UploadRequestFile(
	String id,
	Checksum checksum,
	long size,
	Instant created,
	Instant lastModified
) {
	public record WithPath(UploadRequestFile file, Path path) {
	}

	public static final Codec<UploadRequestFile> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(UploadRequestFile::id),
		Checksum.CODEC.fieldOf("checksum").forGetter(UploadRequestFile::checksum),
		Codec.LONG.fieldOf("size").forGetter(UploadRequestFile::size),
		KLibCodecs.INSTANT.fieldOf("created").forGetter(UploadRequestFile::created),
		KLibCodecs.INSTANT.fieldOf("last_modified").forGetter(UploadRequestFile::lastModified)
	).apply(i, UploadRequestFile::new));

	public static WithPath load(String id, Path path, @Nullable ProgressItem progressItem) throws IOException {
		var attributes = Files.readAttributes(path, BasicFileAttributes.class);
		long size = attributes.size();

		return new WithPath(new UploadRequestFile(
			id,
			SHA256.TYPE.digest(new FileInfo(path, "", size), progressItem),
			size,
			attributes.creationTime().toInstant(),
			attributes.lastModifiedTime().toInstant()
		), path);
	}

	public static List<WithPath> loadDirectory(String prefix, Path root, @Nullable ProgressItem progressItem) throws IOException {
		var list = new ArrayList<WithPath>();

		try (var stream = Files.walk(root)) {
			for (var path : stream.filter(Files::isRegularFile).toList()) {
				if (Files.size(path) > 0L) {
					list.add(load(prefix + root.relativize(path).toString().replace('\\', '/'), path, progressItem));
				}
			}
		}

		return list;
	}
}
