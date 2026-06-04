package dev.latvian.mods.vidlib.feature.auto;

import net.minecraft.client.renderer.texture.TextureManager;

import java.util.concurrent.Executor;

public record TextureReloadParams(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) {
}
