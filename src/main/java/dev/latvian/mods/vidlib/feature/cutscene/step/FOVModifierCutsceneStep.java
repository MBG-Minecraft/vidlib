package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class FOVModifierCutsceneStep extends CutsceneStep {
	public static final Codec<FOVModifierCutsceneStep> CODEC = KNumber.CODEC.xmap(FOVModifierCutsceneStep::new, s -> s.fov);

	public static final StreamCodec<RegistryFriendlyByteBuf, FOVModifierCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, o -> o.fov,
		FOVModifierCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final ImBuilder<KNumber> fov = KNumberImBuilder.create(1D);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof FOVModifierCutsceneStep s) {
				fov.set(s.fov);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return fov.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return fov.isValid();
		}

		@Override
		public CutsceneStep build() {
			return new FOVModifierCutsceneStep(fov.build());
		}
	}

	public final KNumber fov;

	public FOVModifierCutsceneStep(KNumber fov) {
		this.fov = fov;
	}

	public FOVModifierCutsceneStep() {
		this.fov = KNumber.ONE;
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.FOV_MODIFIER;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevFovMod = state.fovMod;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = fov.getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.fovMod = v;
		}
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
