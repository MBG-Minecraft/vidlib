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

public class BarVisibilityCutsceneStep extends CutsceneStep {
	public static final Codec<BarVisibilityCutsceneStep> CODEC = KNumber.CODEC.xmap(BarVisibilityCutsceneStep::new, s -> s.visibility);

	public static final StreamCodec<RegistryFriendlyByteBuf, BarVisibilityCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, o -> o.visibility,
		BarVisibilityCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final ImBuilder<KNumber> visibility = KNumberImBuilder.create(1D);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof BarVisibilityCutsceneStep s) {
				visibility.set(s.visibility);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return visibility.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return visibility.isValid();
		}

		@Override
		public CutsceneStep build() {
			return new BarVisibilityCutsceneStep(visibility.build());
		}
	}

	public final KNumber visibility;

	public BarVisibilityCutsceneStep(KNumber visibility) {
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
		var v = visibility.getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.barVisibility = (float) v;
		}
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
