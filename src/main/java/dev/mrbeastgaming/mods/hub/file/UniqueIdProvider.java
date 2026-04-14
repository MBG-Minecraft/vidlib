package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.MD5;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.UUID;

public interface UniqueIdProvider {
	interface OfData extends UniqueIdProvider {
		boolean write(FileInfo fileInfo, HubProjectConfig projectConfig, DataOutput data) throws Exception;

		@Override
		@Nullable
		default MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) {
			try (var bytes = new ByteArrayOutputStream();
				 var data = new DataOutputStream(bytes)
			) {
				if (!write(fileInfo, projectConfig, data)) {
					return null;
				}

				data.writeInt(projectConfig.projectId().raw());
				return MD5.fromBytes(MessageDigest.getInstance("MD5").digest(bytes.toByteArray()));
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
		return UniqueIdProvider.ofData((fileInfo, projectConfig, data) -> {
			data.writeLong(id.getMostSignificantBits());
			data.writeLong(id.getLeastSignificantBits());
			IOUtils.writeUTF(data, fileInfo.name());
			return true;
		});
	}

	@Nullable
	MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception;
}
