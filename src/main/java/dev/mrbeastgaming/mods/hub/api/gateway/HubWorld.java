package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.bytes.DataByteOutput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public record HubWorld(
	String id,
	String name,
	String path,
	long size,
	Instant created,
	Instant lastModified,
	Checksum icon
) {
	public static final Codec<HubWorld> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("id").forGetter(HubWorld::id),
		Codec.STRING.fieldOf("name").forGetter(HubWorld::name),
		Codec.STRING.fieldOf("path").forGetter(HubWorld::path),
		Codec.LONG.fieldOf("size").forGetter(HubWorld::size),
		KLibCodecs.INSTANT.fieldOf("created").forGetter(HubWorld::created),
		KLibCodecs.INSTANT.fieldOf("last_modified").forGetter(HubWorld::lastModified),
		Checksum.CODEC.optionalFieldOf("icon", NoChecksum.INSTANCE).forGetter(HubWorld::icon)
	).apply(i, HubWorld::new));

	public static final Codec<List<HubWorld>> LIST_CODEC = CODEC.listOf();

	public void write(DataByteOutput bytes) throws IOException {
		bytes.writeUTF(id);
		bytes.writeUTF(name);
		bytes.writeUTF(path);
		bytes.writeVarLong(size);
		bytes.writeExactTime(created);
		bytes.writeExactTime(lastModified);
		icon.writeFully(bytes);
	}
}
