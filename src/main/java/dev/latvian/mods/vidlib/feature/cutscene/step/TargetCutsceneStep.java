package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TargetCutsceneStep extends CutsceneStep {
	public static final Codec<TargetCutsceneStep> CODEC = KVector.CODEC.xmap(TargetCutsceneStep::new, s -> s.position);

	public static final StreamCodec<RegistryFriendlyByteBuf, TargetCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KVector.STREAM_CODEC, o -> o.position,
		TargetCutsceneStep::new
	);
	public final Ref<KVector> position;

	public TargetCutsceneStep(Ref<KVector> position) {
		this.position = position;
	}

	public TargetCutsceneStep() {
		this.position = KVector.ZERO;
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.TARGET;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevTarget = state.target;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = position.value().get(ctx);

		if (v != null) {
			state.target = v;
			ctx.targetPos = v;
		}
	}
}
