package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BarVisibilityCutsceneStep extends CutsceneStep {
	public static final Codec<BarVisibilityCutsceneStep> CODEC = KNumber.CODEC.xmap(BarVisibilityCutsceneStep::new, s -> s.visibility);

	public static final StreamCodec<RegistryFriendlyByteBuf, BarVisibilityCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, o -> o.visibility,
		BarVisibilityCutsceneStep::new
	);

	public final Ref<KNumber> visibility;

	public BarVisibilityCutsceneStep(Ref<KNumber> visibility) {
		this.visibility = visibility;
	}

	public BarVisibilityCutsceneStep() {
		this.visibility = KNumber.ONE;
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.BAR_VISIBILITY;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevBarVisibility = state.barVisibility;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = visibility.value().getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.barVisibility = (float) v;
		}
	}
}
