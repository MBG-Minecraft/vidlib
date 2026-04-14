package dev.mrbeastgaming.mods.hub.file;

@FunctionalInterface
public interface HubFileUploadFilter {
	boolean upload(FileInfo fileInfo) throws Exception;
}
