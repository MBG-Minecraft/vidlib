package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class FOVModifierCutsceneStep extends CutsceneStep {
	public static final Codec<FOVModifierCutsceneStep> CODEC = KNumber.CODEC.xmap(FOVModifierCutsceneStep::new, s -> s.fov);

	public static final StreamCodec<RegistryFriendlyByteBuf, FOVModifierCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, o -> o.fov,
		FOVModifierCutsceneStep::new
	);

	public final Ref<KNumber> fov;

	public FOVModifierCutsceneStep(Ref<KNumber> fov) {
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
		var v = fov.value().getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.fovMod = v;
		}
	}
}
