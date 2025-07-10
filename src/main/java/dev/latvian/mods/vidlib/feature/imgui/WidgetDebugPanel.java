package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterImBuilder;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.particle.ParticleOptionsImBuilder;
import dev.latvian.mods.vidlib.feature.prop.builtin.highlight.TerrainHighlightProp;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundDataImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.kvector.DynamicKVector;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.flag.ImGuiItemFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class WidgetDebugPanel extends AdminPanel {
	public static final WidgetDebugPanel INSTANCE = new WidgetDebugPanel();

	public final ImInt intData = new ImInt();
	public final int[] int2Data = new int[2];
	public final int[] int3Data = new int[3];
	public final int[] int4Data = new int[4];
	public final ImFloat floatData = new ImFloat();
	public final float[] float2Data = new float[2];
	public final float[] float3Data = new float[3];
	public final float[] float4Data = new float[4];
	public final ImVec2 vec2Data = new ImVec2();
	public final ImVec4 vec4Data = new ImVec4();
	public final ImDouble doubleData = new ImDouble();
	public final double[] double2Data = new double[2];
	public final double[] double3Data = new double[3];
	public final double[] double4Data = new double[4];
	public final ImString stringData = new ImString();
	public final ImString multiLineStringData = new ImString();
	public final ImBoolean booleanData = new ImBoolean();
	public final Easing[] easingData = new Easing[1];
	public final GradientImBuilder gradient1 = new GradientImBuilder();
	public final GradientImBuilder gradient2 = new GradientImBuilder();
	public final ImBuilder<KNumber> numberBuilder = KNumberImBuilder.create(0D);
	public final ImBuilder<KVector> vectorBuilder = KVectorImBuilder.create();
	public final ImBuilder<EntityFilter> entityFilterBuilder = EntityFilterImBuilder.create();
	public final ImBuilder<BlockFilter> blockFilterBuilder = BlockFilterImBuilder.create();
	public final PositionedSoundDataImBuilder soundBuilder = new PositionedSoundDataImBuilder();
	public final ImBuilder<ParticleOptions> particleBuilder = ParticleOptionsImBuilder.create();

	private WidgetDebugPanel() {
		super("widget-debug", "Widget Debug");
		gradient1.set(Color.BLACK.gradient(Color.WHITE));
		gradient2.set(Color.YELLOW.gradient(Color.RED));
	}

	@Override
	public void content(ImGraphics graphics) {
		var mc = Minecraft.getInstance();

		ImGui.pushItemWidth(-1F);

		ImGui.text("Text");

		graphics.pushStack();

		graphics.setErrorText();
		ImGui.text("Error Text");

		graphics.setWarningText();
		ImGui.text("Warning Text");

		graphics.setSuccessText();
		ImGui.text("Success Text");

		graphics.setInfoText();
		ImGui.text("Info Text");

		graphics.setFontScale(1.5F);
		ImGui.text("1.5x Text");

		graphics.setFontScale(2F);
		ImGui.text("2x Text");

		graphics.popStack();

		ImGui.bullet();
		ImGui.text("Bullet + Text");
		ImGui.bulletText("Bullet Text");
		ImGui.separator();

		ImGui.text("All Icons:");
		ImGui.text("");

		for (var icon : ImIcons.VALUES) {
			ImGui.text(icon + " " + icon.name());
		}

		ImGui.text("");
		ImGui.text("Extra Icons:");
		ImGui.text("");
		var extraIcons = new StringBuilder();

		for (var c : ImIcons.EXTRA_ICONS.get()) {
			if (!extraIcons.isEmpty()) {
				extraIcons.append(' ');
			}

			extraIcons.append(c);
		}

		ImGui.textWrapped(extraIcons.toString());
		ImGui.separator();

		ImGui.button("Button###button");
		ImGui.button("Wide Button###button", -1F, 0F);

		ImGui.text("Small Button");
		ImGui.sameLine();
		ImGui.smallButton("S###small-button");
		ImGui.separator();

		ImGui.text("Arrow Buttons: " + intData.get());
		graphics.pushStack();
		graphics.setItemFlag(ImGuiItemFlags.ButtonRepeat, true);

		if (ImGui.arrowButton("###arrow-button-left", ImGuiDir.Left)) {
			intData.getData()[0]--;
		}

		ImGui.sameLine();

		if (ImGui.arrowButton("###arrow-button-up", ImGuiDir.Up)) {
			intData.getData()[0]++;
		}

		ImGui.sameLine();

		if (ImGui.arrowButton("###arrow-button-down", ImGuiDir.Down)) {
			intData.getData()[0]--;
		}

		ImGui.sameLine();

		if (ImGui.arrowButton("###arrow-button-right", ImGuiDir.Right)) {
			intData.getData()[0]++;
		}

		graphics.popStack();
		ImGui.separator();

		ImGui.checkbox("Checkbox###checkbox", booleanData);
		ImGui.separator();

		ImGui.colorEdit3("Color 3###color-3", float3Data, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.PickerHueWheel);
		ImGui.separator();

		ImGui.colorEdit4("Color 4###color-4", float4Data, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.PickerHueWheel);
		ImGui.separator();

		ImGui.text("Int Slider 1");
		ImGui.sliderInt("###int-slider-1", intData.getData(), 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 2");
		ImGui.sliderInt2("###int-slider-2", int2Data, 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 3");
		ImGui.sliderInt3("###int-slider-3", int3Data, 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 4");
		ImGui.sliderInt4("###int-slider-4", int4Data, 0, 100);
		ImGui.separator();

		ImGui.text("Float Slider 1");
		ImGui.sliderFloat("###float-slider-1", floatData.getData(), 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 2");
		ImGui.sliderFloat2("###float-slider-2", float2Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 3");
		ImGui.sliderFloat3("###float-slider-3", float3Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 4");
		ImGui.sliderFloat4("###float-slider-4", float4Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Text Input");
		ImGui.inputText("###text-input", stringData);
		ImGui.separator();

		ImGui.text("Multi-line Text Input");
		ImGui.inputTextMultiline("###multi-line-text-input", multiLineStringData);
		ImGui.separator();

		ImGui.text("Combo");
		graphics.easingCombo("###combo", easingData);
		ImGui.separator();

		if (ImGui.collapsingHeader("Collapsing Header")) {
			ImGui.text("Hello");
		}

		ImGui.separator();

		if (ImGui.collapsingHeader("Collapsing Header (Closeable)", new ImBoolean(true))) {
			ImGui.text("Hello");
		}

		ImGui.separator();

		ImGui.text("Image");
		var sprite = mc.getBlockAtlas().getSprite(ResourceLocation.withDefaultNamespace("block/campfire_fire"));
		ImGui.image(mc.getBlockAtlas().getTexture().vl$getHandle(), 128F, 128F, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
		ImGui.separator();

		ImGui.text("Line Plot");

		if (ImPlot.beginPlot("Line Plot###line-plot")) {
			var xdata = new Double[100];
			var ydata = new Double[100];

			for (int i = 0; i < 100; i++) {
				xdata[i] = (double) i;
				ydata[i] = Math.sin(i * 0.1);
			}

			ImPlot.plotLine("Sin###1", xdata, ydata);
			ImPlot.endPlot();
		}

		ImGui.separator();

		ImGui.text("Child Window");
		ImGui.beginChild("AAAA###child", -1F, 80F, true, ImGuiWindowFlags.MenuBar);

		if (ImGui.beginMenuBar()) {
			if (ImGui.beginMenu("Menu")) {
				ImGui.menuItem("Example");

				ImGui.endMenu();
			}

			ImGui.endMenuBar();
		}

		ImGui.text("Hi");
		ImGui.endChild();
		ImGui.separator();

		ImGui.text("Popup");

		if (ImGui.button("Open Popup###open-popup")) {
			ImGui.openPopup("###widget-debug-popup");
		}

		if (ImGui.button("Open Modal###open-modal")) {
			ImGui.openPopup("###widget-debug-modal");
		}

		ImGui.separator();

		if (ImGui.button("Open Node Editor###open-node-editor")) {
			ImGui.openPopup("###widget-debug-node-modal");
		}

		ImGui.separator();

		graphics.redTextIf("Gradient 1", !gradient1.isValid());
		ImGui.pushID("###gradient-1");
		gradient1.imgui(graphics);
		// ImGui.endChild();
		ImGui.popID();

		graphics.redTextIf("Gradient 2", !gradient2.isValid());
		ImGui.pushID("###gradient-2");
		gradient2.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("KNumber", !numberBuilder.isValid());
		ImGui.sameLine();
		ImGui.pushID("###knumber");
		numberBuilder.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("KVector", !vectorBuilder.isValid());
		ImGui.sameLine();
		ImGui.pushID("###kvector");
		vectorBuilder.imgui(graphics);
		ImGui.popID();

		if (graphics.inGame && numberBuilder.isValid() && vectorBuilder.isValid()) {
			var ctx = mc.level.getGlobalContext();
			var radius = numberBuilder.build().get(ctx);

			if (radius != null && radius > 0D) {
				if (ImGui.button("Create Danger Highlight###create-danger-highlight")) {
					var pos = vectorBuilder.build();
					var scalen = KNumber.of(radius.floatValue());
					var scale = new DynamicKVector(scalen, KNumber.ZERO, scalen);

					mc.level.getProps().add(TerrainHighlightProp.TYPE, prop -> {
						prop.dynamicPos = pos;
						prop.color = new Color(0xCCFFDD00).withAlpha(100).gradient(Color.RED.withAlpha(100), Easing.QUAD_IN);
						prop.scale = scale;
						prop.lifespan = 60;
					});

					mc.level.getProps().add(TerrainHighlightProp.TYPE, prop -> {
						prop.dynamicPos = pos;
						prop.color = Color.RED.withAlpha(100);
						prop.scale = KVector.ZERO.interpolate(Easing.QUAD_IN, scale);
						prop.lifespan = 60;
					});
				}
			}
		}

		ImGui.separator();

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("Entity Filter", !entityFilterBuilder.isValid());
		ImGui.sameLine();
		ImGui.pushID("###entity-filter");
		entityFilterBuilder.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("Block Filter", !blockFilterBuilder.isValid());
		ImGui.sameLine();
		ImGui.pushID("###block-filter");
		blockFilterBuilder.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		graphics.redTextIf("Positioned Sound", !soundBuilder.isValid());
		ImGui.pushID("###positioned-sound");
		soundBuilder.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("Particle", !particleBuilder.isValid());
		ImGui.sameLine();
		ImGui.pushID("###particle");
		particleBuilder.imgui(graphics);
		ImGui.popID();
		ImGui.separator();

		ImGui.popItemWidth();
	}

	@Override
	public void postContent(ImGraphics graphics) {
		if (!isOpen()) {
			return;
		}

		if (ImGui.beginPopup("Popup###widget-debug-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
			ImGui.text("Hi");
			ImGui.endPopup();
		}

		if (ImGui.beginPopupModal("Modal###widget-debug-modal", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
			ImGui.text("Hi");
			ImGui.endPopup();
		}

		ImGuiViewport viewport = ImGui.getMainViewport();
		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPos().y);
		ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());

		if (ImGui.beginPopupModal("Node Editor###widget-debug-node-modal", new ImBoolean(true), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.MenuBar)) {
			graphics.hideMainMenuBar();

			MenuItem.root((g, list) -> {
				list.add(MenuItem.menu(ImIcons.ADD, "Add", g1 -> List.of(
					MenuItem.item("Number", g2 -> {
					})
				)));

				list.add(MenuItem.menu(ImIcons.EDIT, "Edit", g1 -> List.of(
					MenuItem.item(ImIcons.DELETE, "Delete", g2 -> {
					}),
					MenuItem.item(ImIcons.DELETE, "Delete All", g2 -> {
					})
				)));

				list.add(MenuItem.text(ImIcons.WARNING, "").withTooltip(ImText.error("Root node missing!")));
			}).buildRoot(graphics, false);

			ImNodes.beginNodeEditor();

			int uid = 0;

			ImNodes.beginNode(++uid);
			ImNodes.beginNodeTitleBar();
			ImGui.text("Node A");
			ImNodes.endNodeTitleBar();

			ImGui.setNextItemWidth(200F);
			ImGui.inputText("###node-a-input", stringData);
			ImNodes.beginInputAttribute(++uid);
			ImGui.text("-> In");
			ImNodes.endInputAttribute();

			ImGui.sameLine();

			ImNodes.beginOutputAttribute(++uid);
			ImGui.text("Out ->");
			ImNodes.endInputAttribute();

			ImNodes.endNode();

			ImNodes.beginNode(++uid);
			ImNodes.beginNodeTitleBar();
			ImGui.text("Node B");
			ImNodes.endNodeTitleBar();

			ImGui.setNextItemWidth(200F);
			ImGui.inputDouble("###node-b-input", doubleData);
			ImNodes.beginInputAttribute(++uid);
			ImGui.text("-> In");
			ImNodes.endInputAttribute();

			ImGui.sameLine();

			ImNodes.beginOutputAttribute(++uid);
			ImGui.text("Out ->");
			ImNodes.endInputAttribute();

			ImNodes.endNode();

			ImNodes.endNodeEditor();

			ImGui.endPopup();
		}
	}
}
