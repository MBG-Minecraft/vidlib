package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.MD5;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UniqueIdProvider {
	UniqueIdProvider NIL = (fileInfo, projectConfig) -> NoChecksum.INSTANCE;

	interface OfData extends UniqueIdProvider {
		record OfUUIDAndFileName(UUID id) implements OfData {
			@Override
			public boolean write(FileInfo fileInfo, UploadContext ctx, ByteOutput data) throws Exception {
				data.writeUUID(id);
				data.writeUTF(fileInfo.name());
				return true;
			}
		}

		boolean write(FileInfo fileInfo, UploadContext ctx, ByteOutput data) throws Exception;

		@Override
		@Nullable
		default Checksum getUniqueId(FileInfo fileInfo, UploadContext ctx) {
			try {
				var data = ByteOutput.ofByteBuilder(16);

				if (!write(fileInfo, ctx, data)) {
					return null;
				}

				data.writeInt(ctx.project().id().raw());
				return MD5.TYPE.digest(data.toByteArray());
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}

	static UniqueIdProvider ofData(OfData data) {
		return data;
	}

	static UniqueIdProvider ofUUIDAndFileName(UUID id) {
		return new OfData.OfUUIDAndFileName(id);
	}

	@Nullable
	Checksum getUniqueId(FileInfo fileInfo, UploadContext ctx) throws Exception;
}
