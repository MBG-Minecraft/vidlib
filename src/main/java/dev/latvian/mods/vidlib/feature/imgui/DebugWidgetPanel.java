package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.interpolation.BezierPreset;
import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterImBuilder;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImageImBuilder;
import dev.latvian.mods.vidlib.feature.gallery.ItemIcons;
import dev.latvian.mods.vidlib.feature.gallery.PlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerHeads;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TransformationListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.interpolation.InterpolationImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.item.VisualItemKey;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundDataImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumberNodeImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.hub.api.Countries;
import dev.mrbeastgaming.hub.api.Country;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.ImPlotAxisFlags;
import imgui.extension.implot.flag.ImPlotFlags;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.flag.ImGuiItemFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;

import java.util.List;

public class DebugWidgetPanel extends Panel {
	public static final DebugWidgetPanel INSTANCE = new DebugWidgetPanel();

	public final ImInt intData = new ImInt();
	public final ImInt intData2 = new ImInt();
	public final int[] int2Data = new int[2];
	public final int[] int3Data = new int[3];
	public final int[] int4Data = new int[4];
	public final ImFloat floatData = new ImFloat();
	public final ImFloat floatData2 = new ImFloat();
	public final float[] float2Data = new float[2];
	public final float[] float3Data = new float[3];
	public final float[] float4Data = new float[4];
	public final Vector2f bezierP1Data = new Vector2f(0F, 0F);
	public final Vector2f bezierP2Data = new Vector2f(1F, 1F);
	public final BezierPreset[] bezierPreset = new BezierPreset[1];
	public final ImVec2 vec2Data = new ImVec2();
	public final ImVec4 vec4Data = new ImVec4();
	public final ImDouble doubleData = new ImDouble();
	public final double[] double2Data = new double[2];
	public final double[] double3Data = new double[3];
	public final double[] double4Data = new double[4];
	public final ImString stringData = new ImString();
	public final ImString multiLineStringData = new ImString();
	public final ImBoolean booleanData = new ImBoolean();
	public final ImBuilder<Interpolation> interpolationBuilder = InterpolationImBuilder.create();
	public final GradientImBuilder gradient1 = new GradientImBuilder();
	public final GradientImBuilder gradient2 = new GradientImBuilder();
	public final ImBuilder<KNumber> numberBuilder = KNumberImBuilder.create(0D);
	public final ImBuilder<KVector> vectorBuilder = KVectorImBuilder.create();
	public final ImBuilder<EntityFilter> entityFilterBuilder = EntityFilterImBuilder.create();
	public final ImBuilder<BlockFilter> blockFilterBuilder = BlockFilterImBuilder.create();
	public final PositionedSoundDataImBuilder soundBuilder = new PositionedSoundDataImBuilder();
	public final ImBuilder<ParticleOptions> particleBuilder = ParticleOptionsImBuilder.create();
	public final GameProfileImBuilder profileBuilder = new GameProfileImBuilder();
	public final TransformationListImBuilder transformationListBuilder = new TransformationListImBuilder();
	public final ImBuilder<KNumber> numberBuilder2 = new KNumberNodeImBuilder();
	public final GalleryImageImBuilder galleryImageBuilder = new GalleryImageImBuilder(
		List.of(Pins.GALLERY, PlayerBodies.GALLERY, PlayerHeads.GALLERY),
		List.of(Pins.UPLOADER, PlayerBodies.UPLOADER, PlayerHeads.UPLOADER)
	);

	public ItemStack currentStack = ItemStack.EMPTY;
	public VisualItemKey currentStackKey = VisualItemKey.AIR;

	private DebugWidgetPanel() {
		super("widget-debug", "Widget Debug");
		gradient1.set(Color.BLACK.gradient(Color.WHITE));
		gradient2.set(Color.YELLOW.gradient(Color.RED));
	}

	@Override
	public void content(ImGraphics graphics) {
		var mc = Minecraft.getInstance();
		var ctx = mc.level == null ? new KNumberContext() : mc.level.getGlobalContext().fork(null);
		ctx.progress = 0.5D;

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

		for (var variant : ImColorVariant.VALUES) {
			graphics.pushStack();
			graphics.setText(variant);
			ImGui.text(variant.displayName + " Text");
			graphics.popStack();
		}

		ImGui.separator();

		ImGui.text("All Icons:");

		float maxX = ImGui.getContentRegionMaxX() - ImGui.getStyle().getFramePaddingX() - ImGui.getStyle().getScrollbarSize();

		for (var icon : ImIcons.VALUES) {
			if (icon.icon == 0) {
				ImGui.newLine();
				ImGui.text(icon.toString());
			} else {
				if (ImGui.getCursorPosX() > maxX) {
					ImGui.newLine();
				}

				ImGui.text(icon.toString());
				ImGui.sameLine();

				if (ImGui.isItemHovered()) {
					ImGui.beginTooltip();
					graphics.pushStack();
					graphics.setFontScale(2F);
					ImGui.text(icon.toString());
					graphics.popStack();
					ImGui.text(icon.name());
					ImGui.endTooltip();
				}
			}
		}

		ImGui.newLine();
		ImGui.newLine();
		ImGui.text("Extra Icons:");
		ImGui.newLine();
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

		for (var variant : ImColorVariant.VALUES) {
			graphics.button(variant.displayName + " Button###button-variant-" + variant.id, variant);
		}

		ImGui.separator();

		for (var variant : ImColorVariant.VALUES) {
			graphics.smallButton("Small " + variant.displayName + " Button###small-button-variant-" + variant.id, variant);
		}

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

		ImGui.colorEdit4("Color 4###color-4", float4Data, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.PickerHueWheel);
		ImGui.separator();

		ImGui.text("Int Slider 1");
		ImGui.sliderInt("###int-slider-1", intData.getData(), 0, 20);
		ImGui.separator();

		ImGui.text("Int Slider 1 (2)");
		ImGui.sliderInt("###int-slider-1-2", intData2.getData(), 0, 20);
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

		ImGui.text("Range Int Slider 2");
		ImGuiUtils.rangeSliderInt("###range-int-slider-2", intData, intData2, 0, 20, null);
		ImGui.separator();

		ImGui.text("Float Slider 1");
		ImGui.sliderFloat("###float-slider-1", floatData.getData(), 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 1 (2)");
		ImGui.sliderFloat("###float-slider-1-2", floatData2.getData(), 0F, 1F);
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

		ImGui.text("Range Float Slider 2");
		ImGuiUtils.rangeSliderFloat("###range-float-slider-2", floatData, floatData2, 0F, 1F, null);
		ImGui.separator();

		ImGui.text("Text Input");
		ImGui.inputText("###text-input", stringData);
		ImGui.separator();

		if (ImGui.button("Open File Dialog")) {
			stringData.set(AsyncFileSelector.openFileDialog(null, "").join());
		}

		ImGui.separator();

		ImGui.text("Multi-line Text Input");
		ImGui.inputTextMultiline("###multi-line-text-input", multiLineStringData);
		ImGui.separator();

		interpolationBuilder.imguiKey(graphics, "Interpolation", "interpolation");
		ImGui.separator();

		if (graphics.collapsingHeader("Collapsing Header", 0)) {
			ImGui.text("Hello");
		}

		ImGui.separator();

		if (graphics.collapsingHeader("Collapsing Header (Closeable, No Push)", new ImBoolean(true), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
			ImGui.text("Hello");
		}

		ImGui.separator();

		ImGui.text("Image");
		var sprite = mc.getBlockAtlas().getSprite(ResourceLocation.withDefaultNamespace("block/campfire_fire"));
		ImGui.image(mc.getBlockAtlas().getTexture().vl$getHandle(), 128F, 128F, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
		ImGui.separator();
		ImGui.image(ImGuiHooks.imGuiGl3.gFontTexture, 128F, 128F, 0F, 0F, 1F, 1F);

		if (ImGui.isItemHovered()) {
			float h = 1024F;
			ImGui.beginTooltip();
			ImGui.image(ImGuiHooks.imGuiGl3.gFontTexture, h * ImGuiHooks.imGuiGl3.glFontWidth / (float) ImGuiHooks.imGuiGl3.glFontHeight, h, 0F, 0F, 1F, 1F);
			ImGui.endTooltip();
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
		gradient1.imguiKey(graphics, "Gradient 1", "gradient-1");
		gradient2.imguiKey(graphics, "Gradient 2", "gradient-2");
		ImGui.separator();
		numberBuilder.imguiKey(graphics, "KNumber", "knumber");
		ImGui.text("Result: " + (numberBuilder.isValid() ? String.valueOf(numberBuilder.build().get(ctx)) : "Invalid"));
		ImGui.separator();
		vectorBuilder.imguiKey(graphics, "KVector", "kvector");
		ImGui.text("Result: " + (vectorBuilder.isValid() ? String.valueOf(vectorBuilder.build().get(ctx)) : "Invalid"));

		ImGui.separator();
		entityFilterBuilder.imguiKey(graphics, "Entity Filter", "entity-filter");
		ImGui.separator();
		blockFilterBuilder.imguiKey(graphics, "Block Filter", "block-filter");
		ImGui.separator();
		soundBuilder.imguiKey(graphics, "Positioned Sound", "positioned-sound");
		ImGui.separator();
		particleBuilder.imguiKey(graphics, "Particle", "particle");
		ImGui.separator();
		profileBuilder.imguiKey(graphics, "Profile", "profile");
		ImGui.separator();
		transformationListBuilder.imguiKey(graphics, "Transformation List", "transformation-list");
		ImGui.separator();
		numberBuilder2.imguiKey(graphics, "KNumber Node Editor", "knumber-node-editor");
		ImGui.text("Result: " + (numberBuilder2.isValid() ? String.valueOf(numberBuilder2.build().get(ctx)) : "Invalid"));
		ImGui.separator();
		galleryImageBuilder.imguiKey(graphics, "Gallery Image", "gallery-image");
		var selectedGalleryImage = galleryImageBuilder.isValid() ? galleryImageBuilder.build() : null;
		graphics.imageButton(selectedGalleryImage == null ? null : selectedGalleryImage.textureId(), 256F, 256F, UV.FULL, 2, null);

		/*
		if (mc.player != null) {
			graphics.imageButton(PlayerHeads.RENDER_TARGET.get().getColorTexture(), 256F, 256F, UV.FULL, 2, null);
			PlayerHeads.render(mc, PlayerHeads.RENDER_TYPE, mc.player.getUUID(), 0.38F);
		}
		 */

		ImGui.separator();

		if (mc.screen instanceof AbstractContainerScreen<?> screen && screen.getSlotUnderMouse() != null && !screen.getSlotUnderMouse().getItem().isEmpty()) {
			currentStack = screen.getSlotUnderMouse().getItem();
			currentStackKey = VisualItemKey.of(currentStack, mc.level == null ? MiscUtils.STATIC_REGISTRY_ACCESS : mc.level.registryAccess());
		}

		if (currentStackKey != VisualItemKey.AIR) {
			ItemIcons.render(mc, currentStackKey);
		}

		var sink = new FormattedCharSinkPartBuilder();
		graphics.mc.font.split(currentStack.getHoverName(), Integer.MAX_VALUE).getFirst().accept(sink);
		graphics.text(sink.build());

		graphics.pushStack();
		graphics.setFontScale(0.75F);
		ImGui.text(currentStackKey.toString());
		graphics.popStack();

		var currentStackTex = ItemIcons.getTexture(mc, currentStackKey);
		graphics.imageButton(currentStackTex.getTexture(), 48F, 48F, UV.FULL, 2, null);

		ImGui.separator();

		ImGui.text("Bezier");
		Bezier.draw("###bezier", bezierP1Data, bezierP2Data, bezierPreset, 128F, 64);
		ImGui.separator();

		ImGui.text("Line Plot");

		// ImPlot.fitNextPlotAxes();

		if (ImPlot.beginPlot("Line Plot###line-plot", "X", "Sin", new ImVec2(300F, 300F), ImPlotFlags.CanvasOnly, ImPlotAxisFlags.NoLabel, ImPlotAxisFlags.NoLabel)) {
			var xdata = new Double[100];
			var ydata = new Double[100];

			for (int i = 0; i < 100; i++) {
				float t = i / 100F;

				xdata[i] = (double) t * 100D;
				ydata[i] = (double) KMath.linearizedBezierY(t, bezierP1Data.x, bezierP1Data.y, bezierP2Data.x, bezierP2Data.y) * 100D;
			}

			ImPlot.plotLine("Sin###1", xdata, ydata);
			ImPlot.endPlot();
		}

		ImGui.separator();

		ImGui.text("LV: " + Countries.LV.get().displayName());
		graphics.combo("###country", new Country[1], Countries.LOADED.get().byCode().values().toArray(new Country[0]), Country::displayName);

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

		if (ImGui.beginPopupModal("Modal###widget-debug-modal", new ImBoolean(true), ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.AlwaysAutoResize)) {
			ImGui.text("Hi");
			ImGui.endPopup();
		}
	}
}
