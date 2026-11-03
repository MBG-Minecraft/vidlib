package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.math.AAFBB;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.ColoredShape;
import dev.latvian.mods.klib.shape.PositionedColoredShape;
import dev.latvian.mods.klib.shape.VoxelShapeBox;
import dev.latvian.mods.klib.visual.LineVisual;
import dev.latvian.mods.klib.visual.Visual;
import dev.latvian.mods.klib.visual.VisualVertex;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

public record Visuals(List<Visual> visuals) {
	public static final Visuals NONE = new Visuals(List.of());
	public static final Visuals EXTRACT = new Visuals();
	public static final Visuals TICK = new Visuals();

	public Visuals() {
		this(new ArrayList<>(0));
	}

	public boolean hasAny() {
		return !visuals.isEmpty();
	}

	public void clear() {
		visuals.clear();
	}

	public void add(Visual visual) {
		visuals.add(visual);
	}

	public void add(PositionedColoredShape shape) {
		add(shape.asVisual());
	}

	public void addCube(Ref<KVector> pos, VoxelShapeBox shape, Ref<Gradient> quadColor, Ref<Gradient> lineColor) {
		add(new ColoredShape(shape.ref(), quadColor, lineColor).at(pos));
	}

	public void addDebugCube(double x, double y, double z, AAFBB box, boolean selected) {
		addCube(KVector.of(x, y, z).ref(), VoxelShapeBox.of(selected ? box.inflate(0.0625F) : box), Gradient.EMPTY, selected ? Gradient.YELLOW : Gradient.WHITE);
	}

	public void addLine(Ref<KVector> startPos, Ref<KVector> endPos, Ref<Gradient> startColor, Ref<Gradient> endColor) {
		visuals.add(new LineVisual(new VisualVertex(startPos, startColor), new VisualVertex(endPos, endColor), KNumber.PROGRESS));
	}

	public void addOutline(PositionedColoredShape shape) {
		// FIXME entity outline-like rendering of visual, requires shaders
	}

	public boolean contains(Vec3i pos) {
		for (var visual : visuals) {
			if (visual.containsBlock(pos)) {
				return true;
			}
		}

		return false;
	}
}
