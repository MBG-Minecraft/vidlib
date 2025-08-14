package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import dev.latvian.mods.vidlib.math.transform.RotateTransformation;
import dev.latvian.mods.vidlib.math.transform.ScaleTransformation;
import dev.latvian.mods.vidlib.math.transform.Transformation;
import dev.latvian.mods.vidlib.math.transform.TranslateTransformation;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TransformationListImBuilder implements ImBuilder<List<Transformation>> {
	public interface BuilderTransformationStep {
		String getLabel();

		ImUpdate imgui(ImGraphics graphics);

		Transformation build(KNumberContext ctx);

		boolean isValid();
	}

	public record TranslationStep(ImBuilder<KVector> translation) implements BuilderTransformationStep {
		@Override
		public String getLabel() {
			return "Translation";
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return translation.imgui(graphics);
		}

		@Override
		public Transformation build(KNumberContext ctx) {
			var v = translation.build().get(ctx);

			if (v != null) {
				return Transformation.translate(Vec3f.of(v));
			}

			return Transformation.NONE;
		}

		@Override
		public boolean isValid() {
			return translation.isValid();
		}
	}

	public record RotationStep(ImBuilder<KNumber> degrees, Direction.Axis axis) implements BuilderTransformationStep {
		public static final String[] LABELS = {
			"X Rotation",
			"Y Rotation",
			"Z Rotation"
		};

		@Override
		public String getLabel() {
			return LABELS[axis.ordinal()];
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return degrees.imgui(graphics);
		}

		@Override
		public Transformation build(KNumberContext ctx) {
			var v = degrees.build().get(ctx);

			if (v != null) {
				return Transformation.rotate(v.floatValue(), axis);
			}

			return Transformation.NONE;
		}

		@Override
		public boolean isValid() {
			return degrees.isValid();
		}
	}

	public record ScaleStep(ImBuilder<KVector> scale) implements BuilderTransformationStep {
		@Override
		public String getLabel() {
			return "Scale";
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return scale.imgui(graphics);
		}

		@Override
		public Transformation build(KNumberContext ctx) {
			var v = scale.build().get(ctx);

			if (v != null) {
				return Transformation.scale(Vec3f.of(v));
			}

			return Transformation.NONE;
		}

		@Override
		public boolean isValid() {
			return scale.isValid();
		}
	}

	private final List<Transformation> transformationList = new ArrayList<>(1);
	private final List<BuilderTransformationStep> steps = new ArrayList<>(1);

	public TransformationListImBuilder() {
		steps.add(new TranslationStep(KVectorImBuilder.create()));
	}

	@Override
	public void set(List<Transformation> value) {
		transformationList.clear();
		transformationList.addAll(value);
		steps.clear();

		for (var transformation : transformationList) {
			switch (transformation) {
				case TranslateTransformation t -> steps.add(new TranslationStep(KVectorImBuilder.create(KVector.of(t.pos().x(), t.pos().y(), t.pos().z()))));
				case RotateTransformation t -> steps.add(new RotationStep(KNumberImBuilder.create(t.degrees()), t.axis()));
				case ScaleTransformation t -> steps.add(new ScaleStep(KVectorImBuilder.create(KVector.of(t.scale().x(), t.scale().y(), t.scale().z()))));
				default -> {
				}
			}
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (ImGui.beginTabBar("Tabs")) {
			if (ImGui.beginTabItem("Edit")) {
				for (int i = 0; i < steps.size(); i++) {
					ImGui.separator();
					var step = steps.get(i);
					int moveTo = -2;

					graphics.pushStack();
					graphics.setStyleVar(ImGuiStyleVar.ItemSpacing, 6F, 8F);

					if (graphics.button(ImIcons.REMOVE + "###remove-step-" + i, ImColorVariant.RED)) {
						moveTo = -1;
					}

					ImGuiUtils.hoveredTooltip("Remove Step");

					ImGui.sameLine();

					if (i == 0) {
						ImGui.beginDisabled();
					}

					if (graphics.button(ImIcons.ARROW_UP + "###move-up-" + i, ImColorVariant.DARK_BLUE)) {
						moveTo = i - 1;
					}

					ImGuiUtils.hoveredTooltip("Move Up");

					if (i == 0) {
						ImGui.endDisabled();
					}

					ImGui.sameLine();

					if (i == steps.size() - 1) {
						ImGui.beginDisabled();
					}

					if (graphics.button(ImIcons.ARROW_DOWN + "###move-down-" + i, ImColorVariant.DARK_BLUE)) {
						moveTo = i + 1;
					}

					ImGuiUtils.hoveredTooltip("Move Down");

					if (i == steps.size() - 1) {
						ImGui.endDisabled();
					}

					graphics.popStack();

					ImGui.sameLine();

					ImGui.alignTextToFramePadding();
					ImGui.text(step.getLabel());

					if (moveTo != -2) {
						steps.remove(i);

						if (moveTo != -1) {
							steps.add(moveTo, step);
						}

						update = ImUpdate.FULL;
						break;
					}

					ImGui.pushID(i);
					ImGui.sameLine();
					update = update.or(step.imgui(graphics));
					ImGui.popID();
				}

				ImGui.separator();

				if (graphics.button(ImIcons.ADD + " Add Transformation Step###add-step", ImColorVariant.GREEN)) {
					ImGui.openPopup("###add-step-popup");
				}

				if (ImGui.beginPopup("Add Transformation Step###add-step-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
					if (ImGui.selectable("Translation")) {
						steps.add(new TranslationStep(KVectorImBuilder.create()));
					}

					if (ImGui.selectable("X Rotation")) {
						steps.add(new RotationStep(KNumberImBuilder.create(0D), Direction.Axis.X));
					}

					if (ImGui.selectable("Y Rotation")) {
						steps.add(new RotationStep(KNumberImBuilder.create(0D), Direction.Axis.Y));
					}

					if (ImGui.selectable("Z Rotation")) {
						steps.add(new RotationStep(KNumberImBuilder.create(0D), Direction.Axis.Z));
					}

					if (ImGui.selectable("Scale")) {
						steps.add(new ScaleStep(KVectorImBuilder.create(KVector.ONE)));
					}

					ImGui.endPopup();
				}

				ImGui.endTabItem();

				if (update.isAny()) {
					transformationList.clear();
					var ctx = graphics.mc.level == null ? new KNumberContext(null) : graphics.mc.level.getGlobalContext();

					for (var step : steps) {
						if (step.isValid()) {
							var t = step.build(ctx);

							if (t != Transformation.NONE) {
								transformationList.add(t);
							}
						}
					}
				}
			}

			if (ImGui.beginTabItem("Preview")) {
				ImGui.pushItemWidth(-1F);
				var matrix = new Matrix4f();

				for (var t : transformationList) {
					t.transformMatrix4f(matrix);
				}

				ImGuiUtils.FLOAT4[0] = matrix.m00();
				ImGuiUtils.FLOAT4[1] = matrix.m01();
				ImGuiUtils.FLOAT4[2] = matrix.m02();
				ImGuiUtils.FLOAT4[3] = matrix.m03();
				ImGui.dragFloat4("###m0", ImGuiUtils.FLOAT4, 0F, -Float.MAX_VALUE, Float.MAX_VALUE, "%.4f", ImGuiSliderFlags.NoInput);

				ImGuiUtils.FLOAT4[0] = matrix.m10();
				ImGuiUtils.FLOAT4[1] = matrix.m11();
				ImGuiUtils.FLOAT4[2] = matrix.m12();
				ImGuiUtils.FLOAT4[3] = matrix.m13();
				ImGui.dragFloat4("###m1", ImGuiUtils.FLOAT4, 0F, -Float.MAX_VALUE, Float.MAX_VALUE, "%.4f", ImGuiSliderFlags.NoInput);

				ImGuiUtils.FLOAT4[0] = matrix.m20();
				ImGuiUtils.FLOAT4[1] = matrix.m21();
				ImGuiUtils.FLOAT4[2] = matrix.m22();
				ImGuiUtils.FLOAT4[3] = matrix.m23();
				ImGui.dragFloat4("###m2", ImGuiUtils.FLOAT4, 0F, -Float.MAX_VALUE, Float.MAX_VALUE, "%.4f", ImGuiSliderFlags.NoInput);

				ImGuiUtils.FLOAT4[0] = matrix.m30();
				ImGuiUtils.FLOAT4[1] = matrix.m31();
				ImGuiUtils.FLOAT4[2] = matrix.m32();
				ImGuiUtils.FLOAT4[3] = matrix.m33();
				ImGui.dragFloat4("###m3", ImGuiUtils.FLOAT4, 0F, -Float.MAX_VALUE, Float.MAX_VALUE, "%.4f", ImGuiSliderFlags.NoInput);

				ImGui.popItemWidth();

				ImGui.endTabItem();
			}

			ImGui.endTabBar();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		for (var step : steps) {
			if (!step.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public List<Transformation> build() {
		return transformationList;
	}
}
