package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.fade.FadeImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundDataImBuilder;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberImBuilder;
import dev.latvian.mods.vidlib.math.worldvector.FixedWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVectorImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CutsceneStepImBuilder implements ImBuilder<CutsceneStep> {
	public final CutsceneImBuilder parent;
	public final ImBuilder<WorldNumber> start = WorldNumberImBuilder.create(0D);
	public final ImBuilder<WorldNumber> length = WorldNumberImBuilder.create(1D);
	public final ImBoolean overrideOrigin = new ImBoolean(false);
	public final ImBuilder<WorldVector> origin = WorldVectorImBuilder.create();
	public final ImBoolean overrideTarget = new ImBoolean(false);
	public final ImBuilder<WorldVector> target = WorldVectorImBuilder.create();
	public final ImBoolean overrideFovModifier = new ImBoolean(false);
	public final ImBuilder<WorldNumber> fovModifier = WorldNumberImBuilder.create(1D);
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
		origin.set(value.origin().orElse(FixedWorldVector.ZERO.instance()));
		overrideTarget.set(value.target().isPresent());
		target.set(value.target().orElse(FixedWorldVector.ZERO.instance()));
		overrideFovModifier.set(value.fovModifier().isPresent());
		fovModifier.set(value.fovModifier().orElse(FixedWorldNumber.ONE.instance()));
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

		ImGui.text("Start");
		ImGui.pushID("###start");
		update = update.or(start.imgui(graphics));
		ImGui.popID();

		ImGui.text("Length");
		ImGui.pushID("###length");
		update = update.or(length.imgui(graphics));
		ImGui.popID();

		ImGui.checkbox("Origin###override-origin", overrideOrigin);

		if (overrideOrigin.get()) {
			ImGui.sameLine();
			ImGui.checkbox("Snap###snap-origin", snapOrigin);

			ImGui.pushID("###origin");
			update = update.or(origin.imgui(graphics));
			ImGui.popID();
		}

		ImGui.checkbox("Target###override-target", overrideTarget);

		if (overrideTarget.get()) {
			ImGui.sameLine();
			ImGui.checkbox("Snap###snap-target", snapTarget);

			ImGui.pushID("###target");
			update = update.or(target.imgui(graphics));
			ImGui.popID();
		}

		ImGui.checkbox("FOV Modifier###override-fov-mod", overrideFovModifier);

		if (overrideFovModifier.get()) {
			ImGui.sameLine();
			ImGui.checkbox("Snap###snap-fov", snapFov);

			ImGui.pushID("###fov-mod");
			update = update.or(fovModifier.imgui(graphics));
			ImGui.popID();
		}

		ImGui.checkbox("Status###override-status", overrideStatus);

		if (overrideStatus.get()) {
			ImGui.pushID("###status");
			update = update.or(status.imgui(graphics));
			ImGui.popID();
		}

		ImGui.checkbox("Bars###bars", barsEnabled);

		if (barsEnabled.get()) {
			ImGui.checkbox("Top Text###override-top-bar", overrideTopBar);

			if (overrideTopBar.get()) {
				ImGui.pushID("###top-bar");
				update = update.or(topBar.imgui(graphics));
				ImGui.popID();
			}

			ImGui.checkbox("Bottom Text###override-bottom-bar", overrideBottomBar);

			if (overrideBottomBar.get()) {
				ImGui.pushID("###bottom-bar");
				update = update.or(bottomBar.imgui(graphics));
				ImGui.popID();
			}
		}

		ImGui.text("Shader");
		ImGui.setNextItemWidth(-1F);
		ImGui.inputText("###shader", shader);

		ImGui.checkbox("Fade###fade-enabled", fadeEnabled);

		if (fadeEnabled.get()) {
			ImGui.pushID("###fade");
			update = update.or(fade.imgui(graphics));
			ImGui.popID();
		}

		// sounds

		ImGui.popItemWidth();
		return update;
	}

	@Override
	public boolean isValid() {
		if (start.isValid()
			&& length.isValid()
			&& origin.isValid()
			&& target.isValid()
			&& fovModifier.isValid()
			&& status.isValid()
			&& topBar.isValid()
			&& bottomBar.isValid()
			&& (shader.isEmpty() || ResourceLocation.read(shader.get()).isSuccess())
			&& fade.isValid()
		) {
			for (var sound : sounds) {
				if (!sound.isValid()) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public CutsceneStep build() {
		return new CutsceneStep(
			start.build(),
			length.build(),
			overrideOrigin.get() ? Optional.of(origin.build()) : Optional.empty(),
			overrideTarget.get() ? Optional.of(target.build()) : Optional.empty(),
			overrideFovModifier.get() ? Optional.of(fovModifier.build()) : Optional.empty(),
			overrideStatus.get() ? Optional.of(status.build()) : Optional.empty(),
			barsEnabled.get() ? Optional.of(new CutsceneStepBars(
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
