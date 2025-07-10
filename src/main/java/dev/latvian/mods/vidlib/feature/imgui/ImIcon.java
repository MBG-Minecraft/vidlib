package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.core.VLGpuTexture;
import imgui.ImGui;

public interface ImIcon {
	ImIcon NONE = new ImIcon() {
		@Override
		public String iconName() {
			return "NONE";
		}

		@Override
		public char toChar() {
			return 0;
		}

		@Override
		public String toString() {
			return "";
		}

		@Override
		public String formatLabel(ImGraphics graphics, String label) {
			return label;
		}
	};

	record ImageImIcon(int textureID, float sizeX, float sizeY, float u0, float v0, float u1, float v1) implements ImIcon {
		@Override
		public char toChar() {
			return 0;
		}

		@Override
		public String formatLabel(ImGraphics graphics, String label) {
			ImGui.image(textureID, sizeX, sizeY, u0, v0, u1, v1);
			ImGui.sameLine();
			return label;
		}
	}

	static ImIcon image(VLGpuTexture texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) {
		return new ImageImIcon(texture.vl$getHandle(), sizeX, sizeY, u0, v0, u1, v1);
	}

	static ImIcon image(VLGpuTexture texture, float sizeX, float sizeY) {
		return new ImageImIcon(texture.vl$getHandle(), sizeX, sizeY, 0F, 0F, 1F, 1F);
	}

	static ImIcon image(VLGpuTexture texture, float u0, float v0, float u1, float v1) {
		return image(texture, 18F, 18F, u0, v0, u1, v1);
	}

	static ImIcon image(VLGpuTexture texture) {
		return image(texture, 18F, 18F);
	}

	default String iconName() {
		return toString();
	}

	char toChar();

	default String formatLabel(ImGraphics graphics, String label) {
		if (label.isEmpty()) {
			return toString();
		}

		String builder = String.valueOf(toChar()) +
			' ' +
			label;
		return builder;
	}
}
