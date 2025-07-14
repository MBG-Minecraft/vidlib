package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.fade.FadeImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundDataImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CutsceneStepImBuilder implements ImBuilder<CutsceneStep> {
	public final CutsceneImBuilder parent;
	public final ImInt start = new ImInt();
	public final ImBuilder<KNumber> length = KNumberImBuilder.create(0D);
	public final ImBoolean overrideOrigin = new ImBoolean(false);
	public final ImBuilder<KVector> origin = KVectorImBuilder.create();
	public final ImBoolean overrideTarget = new ImBoolean(false);
	public final ImBuilder<KVector> target = KVectorImBuilder.create();
	public final ImBoolean overrideFovModifier = new ImBoolean(false);
	public final ImBuilder<KNumber> fovModifier = KNumberImBuilder.create(1D);
	public final ImBoolean overrideStatus = new ImBoolean(false);
	public final TextComponentImBuilder status = new TextComponentImBuilder();
	public final ImBoolean barsEnabled = new ImBoolean(false);
	public final ImBoolean overrideTopBar = new ImBoolean(false);
	public final TextComponentImBuilder topBar = new TextComponentImBuilder();
	public final ImBoolean overrideBottomBar = new ImBoolean(false);
	public final TextComponentImBuilder bottomBar = new TextComponentImBuilder();

	public final ImString shader = ImGuiUtils.resizableString();
	public final ImBoolean fadeEnabled = new ImBoolean(false);
	public final FadeImBuilder fade = new FadeImBuilder();
	public final List<PositionedSoundDataImBuilder> sounds = new ArrayList<>(0);
	public final ImBoolean snapOrigin = new ImBoolean(false);
	public final ImBoolean snapTarget = new ImBoolean(false);
	public final ImBoolean snapFov = new ImBoolean(false);
	public boolean delete = false;

	public CutsceneStepImBuilder(CutsceneImBuilder parent) {
		this.parent = parent;
	}

	@Override
	public void set(CutsceneStep value) {
		start.set(value.start());
		length.set(value.length());
		overrideOrigin.set(value.origin().isPresent());
		origin.set(value.origin().orElse(KVector.ZERO));
		overrideTarget.set(value.target().isPresent());
		target.set(value.target().orElse(KVector.ZERO));
		overrideFovModifier.set(value.fovModifier().isPresent());
		fovModifier.set(value.fovModifier().orElse(KNumber.ONE));
		overrideStatus.set(value.status().isPresent());
		status.set(value.status().orElse(Empty.COMPONENT));
		barsEnabled.set(value.bars().isPresent());
		overrideTopBar.set(value.bars().map(CutsceneStepBars::top).isPresent());
		topBar.set(value.bars().flatMap(CutsceneStepBars::top).orElse(Empty.COMPONENT));
		overrideBottomBar.set(value.bars().map(CutsceneStepBars::bottom).isPresent());
		bottomBar.set(value.bars().flatMap(CutsceneStepBars::bottom).orElse(Empty.COMPONENT));
		shader.set(value.shader().map(ResourceLocation::toString).orElse(""));
		fadeEnabled.set(value.fade().isPresent());
		fade.set(value.fade().orElse(Fade.DEFAULT));
		// sounds.clear();
		// sounds.addAll(value.sounds());
		snapOrigin.set(value.snap().origin());
		snapTarget.set(value.snap().target());
		snapFov.set(value.snap().fov());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = ImUpdate.NONE;
		ImGui.pushItemWidth(-1F);

		ImGui.alignTextToFramePadding();
		ImGui.text("Start");
		ImGui.sameLine();
		ImGui.inputInt("###start", start);
		update = update.orItemEdit();

		ImGui.alignTextToFramePadding();
		ImGui.text("Length");
		ImGui.sameLine();
		ImGui.pushID("###length");
		graphics.pushStack();
		graphics.setNumberType(ImNumberType.INT);
		update = update.or(length.imgui(graphics));
		graphics.popStack();
		ImGui.popID();

		ImGui.separator();

		update = update.or(ImGui.checkbox("Origin###override-origin", overrideOrigin));

		if (overrideOrigin.get()) {
			ImGui.sameLine();
			update = update.or(ImGui.checkbox("Snap###snap-origin", snapOrigin));
			ImGui.sameLine();

			ImGui.pushID("###origin");
			update = update.or(origin.imgui(graphics));
			ImGui.popID();
		}

		update = update.or(ImGui.checkbox("Target###override-target", overrideTarget));

		if (overrideTarget.get()) {
			ImGui.sameLine();
			update = update.or(ImGui.checkbox("Snap###snap-target", snapTarget));
			ImGui.sameLine();

			ImGui.pushID("###target");
			update = update.or(target.imgui(graphics));
			ImGui.popID();
		}

		update = update.or(ImGui.checkbox("FOV Modifier###override-fov-mod", overrideFovModifier));

		if (overrideFovModifier.get()) {
			ImGui.sameLine();
			update = update.or(ImGui.checkbox("Snap###snap-fov", snapFov));
			ImGui.sameLine();

			ImGui.pushID("###fov-mod");
			graphics.pushStack();
			graphics.setNumberRange(new Range(0F, 2F));
			update = update.or(fovModifier.imgui(graphics));
			graphics.popStack();
			ImGui.popID();
		}

		ImGui.separator();

		update = update.or(ImGui.checkbox("Status###override-status", overrideStatus));

		if (overrideStatus.get()) {
			ImGui.pushID("###status");
			update = update.or(status.imgui(graphics));
			ImGui.popID();
		}

		update = update.or(ImGui.checkbox("Bars###bars", barsEnabled));

		if (barsEnabled.get()) {
			update = update.or(ImGui.checkbox("Top Text###override-top-bar", overrideTopBar));

			if (overrideTopBar.get()) {
				ImGui.pushID("###top-bar");
				update = update.or(topBar.imgui(graphics));
				ImGui.popID();
			}

			update = update.or(ImGui.checkbox("Bottom Text###override-bottom-bar", overrideBottomBar));

			if (overrideBottomBar.get()) {
				ImGui.pushID("###bottom-bar");
				update = update.or(bottomBar.imgui(graphics));
				ImGui.popID();
			}
		}

		ImGui.text("Shader");
		ImGui.setNextItemWidth(-1F);
		ImGui.inputText("###shader", shader);
		update = update.orItemEdit();

		update = update.or(ImGui.checkbox("Fade###fade-enabled", fadeEnabled));

		if (fadeEnabled.get()) {
			ImGui.pushID("###fade");
			update = update.or(fade.imgui(graphics));
			ImGui.popID();
		}

		ImGui.alignTextToFramePadding();
		graphics.redTextIf("Sounds", !areSoundsValid());
		ImGui.sameLine();

		if (ImGui.button(ImIcons.ADD + "###add-sound")) {
			sounds.add(new PositionedSoundDataImBuilder());
			update = ImUpdate.FULL;
		}

		if (!sounds.isEmpty()) {
			ImGui.sameLine();

			if (ImGui.button("Stop all Sounds###stop-all-sounds")) {
				Minecraft.getInstance().getSoundManager().stop();
			}

			ImGui.pushID("###sounds");

			for (int i = 0; i < sounds.size(); i++) {
				var sound = sounds.get(i);

				ImGui.pushID(i);

				ImGui.text("Sound");
				ImGui.sameLine();

				if (sound.isValid()) {
					if (ImGui.smallButton(ImIcons.PLAY + " Play")) {
						Minecraft.getInstance().playGlobalSound(sound.build(), parent.variables);
					}
				} else {
					ImGui.beginDisabled();
					ImGui.smallButton(ImIcons.PLAY + " Play");
					ImGui.endDisabled();
				}

				ImGui.sameLine();

				graphics.pushStack();
				graphics.setRedButton();

				boolean deleteClicked = ImGui.smallButton(ImIcons.DELETE + " Delete");

				graphics.popStack();

				ImGui.indent();
				update = update.or(sound.imgui(graphics));
				ImGui.unindent();

				if (deleteClicked) {
					sound.delete = true;
				}

				ImGui.popID();
			}

			ImGui.popID();

			if (sounds.removeIf(sound -> sound.delete)) {
				update = ImUpdate.FULL;
			}
		}

		// sounds

		ImGui.popItemWidth();
		return update;
	}

	public boolean areSoundsValid() {
		if (sounds.isEmpty()) {
			return true;
		}

		for (var sound : sounds) {
			if (!sound.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isValid() {
		return length.isValid()
			&& origin.isValid()
			&& target.isValid()
			&& fovModifier.isValid()
			&& status.isValid()
			&& topBar.isValid()
			&& bottomBar.isValid()
			&& (shader.isEmpty() || ResourceLocation.read(shader.get()).isSuccess())
			&& fade.isValid()
			&& areSoundsValid();
	}

	@Override
	public CutsceneStep build() {
		return new CutsceneStep(
			start.get(),
			length.build(),
			overrideOrigin.get() ? Optional.of(origin.build()) : Optional.empty(),
			overrideTarget.get() ? Optional.of(target.build()) : Optional.empty(),
			overrideFovModifier.get() ? Optional.of(fovModifier.build()) : Optional.empty(),
			overrideStatus.get() ? Optional.of(status.build()) : Optional.empty(),
			barsEnabled.get() ? Optional.of(CutsceneStepBars.of(
				overrideTopBar.get() ? Optional.of(topBar.build()) : Optional.empty(),
				overrideBottomBar.get() ? Optional.of(bottomBar.build()) : Optional.empty()
			)) : Optional.empty(),
			shader.get().isEmpty() ? Optional.empty() : Optional.of(ResourceLocation.parse(shader.get())),
			fadeEnabled.get() ? Optional.of(fade.build()) : Optional.empty(),
			sounds.stream().map(PositionedSoundDataImBuilder::build).toList(),
			new CutsceneStepSnap(snapOrigin.get(), snapTarget.get(), snapFov.get()),
			List.of()
		);
	}
}
