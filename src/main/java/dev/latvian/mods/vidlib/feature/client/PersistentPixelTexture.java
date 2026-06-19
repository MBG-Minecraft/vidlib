package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class PersistentPixelTexture extends SimpleTexture implements Dumpable {
	public boolean closed;
	public NativeImage pixels;

	public PersistentPixelTexture(Identifier location) {
		super(location);
		this.closed = false;
		this.pixels = null;
	}

	@Override
	public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
		closed = false;
		return new TextureContents(pixels.mappedCopy(IntUnaryOperator.identity()), null);
	}

	@Override
	public void close() {
		closed = true;
		super.close();

		if (pixels != null) {
			pixels.close();
		}
	}

	@Override
	public void dumpContents(Identifier id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
