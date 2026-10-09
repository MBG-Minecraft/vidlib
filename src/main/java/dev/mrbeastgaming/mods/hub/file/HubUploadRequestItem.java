package dev.mrbeastgaming.mods.hub.file;

import dev.mrbeastgaming.mods.hub.api.data.HubProjectFileLinkInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadRequestFile;
import org.jetbrains.annotations.Nullable;

public record HubUploadRequestItem(
	HubFileInfo info,
	HubUploadRequestFile file,
	@Nullable HubProjectFileLinkInfo link
) {
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
