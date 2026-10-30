package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class OriginCutsceneStep extends CutsceneStep {
	public static final Codec<OriginCutsceneStep> CODEC = KVector.CODEC.xmap(OriginCutsceneStep::new, s -> s.position);

	public static final StreamCodec<RegistryFriendlyByteBuf, OriginCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KVector.STREAM_CODEC, o -> o.position,
		OriginCutsceneStep::new
	);

	public final Ref<KVector> position;

	public OriginCutsceneStep(Ref<KVector> position) {
		this.position = position;
	}

	public OriginCutsceneStep() {
		this.position = KVector.ZERO;
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.ORIGIN;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevOrigin = state.origin;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = position.value().get(ctx);

		if (v != null) {
			state.origin = v;
		}
	}
}
