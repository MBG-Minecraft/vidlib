package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.IOConsumer;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.MD5;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.data.HubFileType;
import dev.mrbeastgaming.mods.hub.api.data.HubPossibleUser;
import dev.mrbeastgaming.mods.hub.api.data.HubProjectFileLinkInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadRequestFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Consumer;

public class HubUploadBuilder {
	public final HubFileInfo info;
	String id;
	String path;
	HubFileType type;
	Checksum uniqueId;
	Instant creationTime;
	HubPossibleUser assignedTo;
	boolean unlinked;
	boolean skip;
	boolean manual;

	public HubUploadBuilder(HubFileInfo info) {
		this.info = info;
		this.id = "";
		this.path = "";
		this.type = null;
		this.uniqueId = NoChecksum.INSTANCE;
		this.creationTime = Instant.now();
		this.assignedTo = HubPossibleUser.NONE;
		this.unlinked = false;
		this.skip = false;
		this.manual = false;
	}

	public void setId(String value) {
		this.id = value;
	}

	public void setPath(String value) {
		this.path = value;
	}

	public void setType(HubFileType value) {
		this.type = value;
	}

	public void setUniqueId(@Nullable Checksum value) {
		if (value != null && !value.isNil()) {
			this.uniqueId = value;
		}
	}

	public void setUniqueId(IOConsumer<ByteOutput> data) throws IOException {
		var byteOutput = ByteOutput.ofByteBuilder();
		data.accept(byteOutput);
		setUniqueId(MD5.TYPE.digest(byteOutput.toByteArray()));
	}

	public void setCreationTime(Instant value) {
		this.creationTime = value;
	}

	public void setAssignedTo(HubPossibleUser value) {
		this.assignedTo = value;
	}

	public void setUnlinked(boolean unlinked) {
		this.unlinked = unlinked;
	}

	public void unlinked() {
		setUnlinked(true);
	}

	public void setSkip(boolean value) {
		skip = value;
	}

	public void skip() {
		setSkip(true);
	}

	public void setManual(boolean value) {
		manual = true;
	}

	public void manual() {
		setManual(true);
	}

	public void build(HubFileInfo info, Consumer<HubUploadRequestItem> items) throws IOException {
		if (skip) {
			return;
		}

		items.accept(new HubUploadRequestItem(info, new HubUploadRequestFile(
			id.isEmpty() ? MD5.TYPE.digest(info.fullPath().getBytes(StandardCharsets.UTF_8)).toString() : id,
			info.checksum(),
			info.size(),
			info.name(),
			creationTime == null ? info.created() : creationTime,
			info.lastModified()
		), unlinked ? null : new HubProjectFileLinkInfo(
			uniqueId,
			info.path(),
			type != null ? type : HubFileType.probe(info.file()),
			assignedTo,
			manual
		)));
	}
}
