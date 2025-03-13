package dev.beast.mods.shimmer.feature.worldsync;

import java.nio.file.Path;
import java.util.UUID;

public record LocalFile(String path, UUID checksum, long size, Path file) {
}