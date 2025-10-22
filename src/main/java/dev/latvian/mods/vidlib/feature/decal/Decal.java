package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.List;

public class Decal {
	public static Color DANGER_INNER_COLOR = Color.ofRGB(0xFFAA00);
	public static Color DANGER_OUTER_COLOR = Color.RED;

	public static Decal createDanger(float width) {
		var d = new Decal();
		d.type = DecalType.DANGER;
		d.start = 0F;
		d.end = width;
		d.startColor = DANGER_INNER_COLOR.withAlpha(100);
		d.endColor = DANGER_OUTER_COLOR.withAlpha(100);
		d.setHeight(0.1875F);
		d.fillSize = 1F;
		d.terrain = true;
		return d;
	}

	public Decal parent;
	public DecalType type;
	public Vector3d position;
	public float start;
	public float end;
	public DecalFillType fillType;
	public float fillSize;
	public float fillThickness;
	public float heightScale;
	public float rotation;
	public Color startColor;
	public Color endColor;
	public boolean surface;
	public boolean terrain;
	public boolean additive;
	public float edges;

	public Decal() {
		this.parent = null;
		this.type = DecalType.SPHERE;
		this.position = new Vector3d();
		this.start = 0F;
		this.end = 1F;
		this.fillType = DecalFillType.SOLID;
		this.fillSize = 1F;
		this.fillThickness = 0.0625F;
		this.heightScale = 1F;
		this.rotation = 0F;
		this.startColor = Color.WHITE;
		this.endColor = Color.WHITE;
		this.surface = false;
		this.terrain = false;
		this.additive = false;
		this.edges = 4F;
	}

	public Decal(Decal other) {
		this.parent = other;
		this.type = other.type;
		this.position = other.position;
		this.start = other.start;
		this.end = other.end;
		this.fillType = other.fillType;
		this.fillSize = other.fillSize;
		this.fillThickness = other.fillThickness;
		this.heightScale = other.heightScale;
		this.rotation = other.rotation;
		this.startColor = other.startColor;
		this.endColor = other.endColor;
		this.surface = other.surface;
		this.terrain = other.terrain;
		this.additive = other.additive;
		this.edges = other.edges;
	}

	public void setPosition(Vector3d pos, boolean joined) {
		if (!joined && parent != null) {
			parent = null;
			position = new Vector3d();
		}

		position.set(pos);
	}

	public void setPosition(Position pos, boolean joined) {
		if (!joined && parent != null) {
			parent = null;
			position = new Vector3d();
		}

		position.set(pos.x(), pos.y(), pos.z());
	}

	public boolean isVisible() {
		return type != DecalType.NONE && end > 0F && (startColor.alpha() > 0 || endColor.alpha() > 0);
	}

	public void addToList(List<Decal> list) {
		if (type == DecalType.DANGER) {
			if (fillSize > 0F) {
				var g = new Decal(this);
				g.type = DecalType.CYLINDER;
				g.startColor = startColor.withAlpha(0);
				g.end = end - fillThickness;
				g.fillType = DecalFillType.GRID;

				if (g.isVisible()) {
					list.add(g);
				}
			}

			var e = new Decal(this);
			e.type = DecalType.CYLINDER;
			e.start = e.end - fillThickness;
			e.startColor = endColor;
			e.fillType = DecalFillType.SOLID;
			e.fillSize = 0F;

			if (e.isVisible()) {
				list.add(e);
			}

			var f = new Decal(this);
			f.type = DecalType.CYLINDER;
			f.fillType = DecalFillType.SOLID;
			f.fillSize = 0F;

			if (f.isVisible()) {
				list.add(f);
			}
		} else if (isVisible()) {
			list.add(this);
		}
	}

	public void upload(IntArrayList arr, Vec3 cameraPos) {
		arr.add((type.shaderId & 7)
			| (surface ? 8 : 0)
			| (terrain ? 16 : 0)
		);

		arr.add(Float.floatToIntBits((float) (position.x - cameraPos.x))); // 1
		arr.add(Float.floatToIntBits((float) (position.y - cameraPos.y))); // 2
		arr.add(Float.floatToIntBits((float) (position.z - cameraPos.z))); // 3

		arr.add(Float.floatToIntBits(start)); // 4
		arr.add(Float.floatToIntBits(end)); // 5
		arr.add(Float.floatToIntBits(heightScale)); // 6
		arr.add(Float.floatToIntBits((float) Math.toRadians(rotation < 0F ? (rotation + 360F) : rotation))); // 7

		arr.add(startColor.argb()); // 8
		arr.add(endColor.argb()); // 9
		arr.add(fillType.shaderId & 7); // 10
		arr.add(Float.floatToIntBits(fillSize)); // 11
		arr.add(Float.floatToIntBits(fillThickness)); // 12
		arr.add(Float.floatToIntBits(edges)); // 13
	}

	public void imgui(ImGraphics graphics, Collection<Decal> decals) {
		ImGui.text("Shape");
		DecalType.UNIT[0] = type;
		graphics.combo("###type", DecalType.UNIT, DecalType.VALUES);
		type = DecalType.UNIT[0];

		if (type == DecalType.REGULAR) {
			ImGuiUtils.FLOAT.set(edges);
			ImGui.sliderFloat("###edges", ImGuiUtils.FLOAT.getData(), 1F, 16F, "%.01f");
			edges = ImGuiUtils.FLOAT.get();
			ImGuiUtils.hoveredTooltip("Edges");
		}

		if (parent != null && !decals.contains(parent)) {
			setPosition(position, false);
		}

		if (parent != null) {
			ImGuiUtils.BOOLEAN.set(true);

			if (ImGui.checkbox(ImIcons.LOCK + " Joined Position###joined-position", ImGuiUtils.BOOLEAN)) {
				setPosition(position, false);
			}
		} else {
			ImGui.text("Position");
			Vector3dImBuilder.imgui(graphics, position, SelectedPosition.UNIT);
		}

		ImGuiUtils.BOOLEAN.set(terrain);

		if (ImGui.checkbox("Terrain###terrain", ImGuiUtils.BOOLEAN)) {
			terrain = ImGuiUtils.BOOLEAN.get();
		}

		ImGui.text("Size");
		graphics.smallText("Width");

		float[] starta = {start};
		float[] enda = {end};
		ImGuiUtils.FLOAT.set(start);
		ImGui.dragFloatRange2("###size-range", starta, enda, 0.0625F, 0F, 100F, "%f");
		start = starta[0];
		end = enda[0];

		ImGuiUtils.hoveredTooltip("Size Range");

		float diff = end - start;
		if (ImGui.dragFloat("###size", starta, 0.0625F, 0F, 100F, "%f")) {
			start = starta[0];
			end = start + diff;
		}

		ImGuiUtils.hoveredTooltip("Size");

		graphics.smallText("Height");

		ImGuiUtils.FLOAT.set(heightScale);
		ImGui.sliderFloat("###height-scale", ImGuiUtils.FLOAT.getData(), 0.125F, 8F, "%f", ImGuiSliderFlags.Logarithmic);
		heightScale = ImGuiUtils.FLOAT.get();

		ImGuiUtils.hoveredTooltip("Height Scale");

		starta[0] = getHeight();

		if (ImGui.dragFloat("###height", starta, 0.0625F, 0F, 100F, "%f")) {
			setHeight(starta[0]);
		}

		ImGuiUtils.hoveredTooltip("Height");

		// start = starta[0];
		// end = start + hdiff;

		Color4ImBuilder.UNIT.set(startColor);
		Color4ImBuilder.UNIT.imguiKey(graphics, "Start Color", "start-color");
		startColor = Color4ImBuilder.UNIT.build();

		Color4ImBuilder.UNIT.set(endColor);
		Color4ImBuilder.UNIT.imguiKey(graphics, "End Color", "end-color");
		endColor = Color4ImBuilder.UNIT.build();

		ImGui.alignTextToFramePadding();
		ImGui.text("Fill");
		ImGui.sameLine();

		DecalFillType.UNIT[0] = fillType;
		graphics.combo("###fill-type", DecalFillType.UNIT, DecalFillType.VALUES);
		fillType = DecalFillType.UNIT[0];

		if (fillType != DecalFillType.SOLID) {
			ImGui.alignTextToFramePadding();
			graphics.smallText("Size");
			ImGui.sameLine();
			ImGuiUtils.FLOAT.set(fillSize);
			ImGui.sliderFloat("###fill-size", ImGuiUtils.FLOAT.getData(), 0F, 4F, "%f");
			fillSize = ImGuiUtils.FLOAT.get();

			ImGui.alignTextToFramePadding();
			graphics.smallText("Thickness");
			ImGui.sameLine();
			ImGuiUtils.FLOAT.set(fillThickness);
			ImGui.sliderFloat("###fill-thickness", ImGuiUtils.FLOAT.getData(), 0F, 0.5F, "%f");
			fillThickness = ImGuiUtils.FLOAT.get();
		}

		ImGui.alignTextToFramePadding();
		ImGui.text("Rotation");
		ImGui.sameLine();
		ImGuiUtils.FLOAT.set(rotation);
		ImGui.sliderFloat("###rotation", ImGuiUtils.FLOAT.getData(), -180F, 180F, "%f");
		rotation = ImGuiUtils.FLOAT.get();
	}

	public float getHeight() {
		return end * heightScale * 2F;
	}

	public void setHeight(float height) {
		heightScale = height / (end * 2F);
	}

	public void applyProgress(float progress) {
		if (progress <= 0F) {
			type = DecalType.NONE;
			return;
		}

		startColor = startColor.withAlpha(startColor.alphaf() * progress);
		endColor = endColor.withAlpha(endColor.alphaf() * progress);
	}
}
