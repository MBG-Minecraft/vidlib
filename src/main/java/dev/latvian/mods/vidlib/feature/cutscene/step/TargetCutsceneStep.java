package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class TargetCutsceneStep extends CutsceneStep {
	public static final Codec<TargetCutsceneStep> CODEC = KVector.CODEC.xmap(TargetCutsceneStep::new, s -> s.position);

	public static final StreamCodec<RegistryFriendlyByteBuf, TargetCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KVector.STREAM_CODEC, o -> o.position,
		TargetCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final ImBuilder<KVector> position = KVectorImBuilder.create(KVector.ZERO);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof TargetCutsceneStep s) {
				position.set(s.position);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return position.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return position.isValid();
		}

		@Override
		public CutsceneStep build() {
			return new TargetCutsceneStep(position.build());
		}
	}

	public final KVector position;

	public TargetCutsceneStep(KVector position) {
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
		var v = position.get(ctx);

		if (v != null) {
			state.target = v;
			ctx.targetPos = v;
		}
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
