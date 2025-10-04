package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class DataTexture extends AbstractTexture implements Dumpable {
	private final String name;
	private int width;
	private int height;
	private final List<IntArrayList> pixels;
	private NativeImage image;
	private int currentRow;
	private int columnCount;

	public DataTexture(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.texture = RenderSystem.getDevice().createTexture(name, TextureFormat.RGBA8, width, height, 1);
		texture.setTextureFilter(FilterMode.LINEAR, false);
		this.pixels = new ArrayList<>(height);
	}

	@Override
	public void close() {
		super.close();

		if (image != null) {
			image.close();
			image = null;
		}
	}

	public void beginUpdate() {
		currentRow = 0;
		columnCount = 0;
	}

	private void countPrevRow() {
		if (currentRow > 0) {
			columnCount = Math.max(columnCount, pixels.get(currentRow - 1).size());
		}
	}

	public IntArrayList nextRow() {
		countPrevRow();
		IntArrayList row;

		if (currentRow >= pixels.size()) {
			row = new IntArrayList(width);
			pixels.add(row);
		} else {
			row = pixels.get(currentRow);
			row.clear();
		}

		currentRow++;
		return row;
	}

	public void endUpdate() {
		countPrevRow();

		if (image == null || columnCount > width || currentRow > height) {
			width = Mth.ceil((columnCount + 1) / 4D) * 4;
			height = Mth.ceil((currentRow + 1) / 4D) * 4;

			if (image != null) {
				image.close();
				image = null;
			}

			image = new NativeImage(width, height, false);

			if (texture != null) {
				texture.close();
			}

			texture = RenderSystem.getDevice().createTexture(name, TextureFormat.RGBA8, width, height, 1);
			texture.setTextureFilter(FilterMode.LINEAR, false);
		}

		for (int y = 0; y < currentRow; y++) {
			var list = pixels.get(y);

			for (int x = 0; x < list.size(); x++) {
				image.setPixel(x, y, list.getInt(x));
			}
		}

		RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image, 0, 0, 0, columnCount, currentRow, 0, 0);
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
