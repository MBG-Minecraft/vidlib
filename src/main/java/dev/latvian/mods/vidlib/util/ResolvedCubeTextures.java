package dev.latvian.mods.vidlib.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ResolvedCubeTextures(@Nullable TerrainRenderLayer commonLayer, List<FaceTexture> faces) {
	public static final ResolvedCubeTextures EMPTY = new ResolvedCubeTextures(null, List.of(FaceTexture.EMPTY, FaceTexture.EMPTY, FaceTexture.EMPTY, FaceTexture.EMPTY, FaceTexture.EMPTY, FaceTexture.EMPTY));

	public static TerrainRenderLayer commonLayer(List<FaceTexture> faces) {
		TerrainRenderLayer commonLayer = null;

		for (var face : faces) {
			if (face == FaceTexture.EMPTY) {
				continue;
			}

			if (commonLayer == null) {
				commonLayer = face.layer();
			} else if (commonLayer != face.layer()) {
				commonLayer = null;
				break;
			}
		}

		return commonLayer;
	}

	public static ResolvedCubeTextures resolve(CubeTextures cube) {
		if (cube == CubeTextures.EMPTY) {
			return EMPTY;
		}

		var all = cube.all().orElse(FaceTexture.EMPTY);

		var faces = List.of(
			cube.down().orElse(all),
			cube.up().orElse(all),
			cube.north().orElse(all),
			cube.south().orElse(all),
			cube.west().orElse(all),
			cube.east().orElse(all)
		);

		return new ResolvedCubeTextures(commonLayer(faces), faces);
	}

	public ResolvedCubeTextures merge(ResolvedCubeTextures other) {
		if (other == EMPTY) {
			return this;
		} else if (this == EMPTY) {
			return other;
		}

		var faces = List.of(
			this.faces.get(0).merge(other.faces.get(0)),
			this.faces.get(1).merge(other.faces.get(1)),
			this.faces.get(2).merge(other.faces.get(2)),
			this.faces.get(3).merge(other.faces.get(3)),
			this.faces.get(4).merge(other.faces.get(4)),
			this.faces.get(5).merge(other.faces.get(5))
		);

		return new ResolvedCubeTextures(commonLayer(faces), faces);
	}

	public ResolvedCubeTextures filter(TerrainRenderLayer filter) {
		if (commonLayer != null && commonLayer == filter) {
			return this;
		}

		boolean empty = true;
		var faces = new ArrayList<FaceTexture>(6);

		for (var face : this.faces) {
			if (face != FaceTexture.EMPTY && face.layer() == filter) {
				faces.add(face);
				empty = false;
			} else {
				faces.add(FaceTexture.EMPTY);
			}
		}

		return empty ? EMPTY : new ResolvedCubeTextures(filter, faces);
	}
}
