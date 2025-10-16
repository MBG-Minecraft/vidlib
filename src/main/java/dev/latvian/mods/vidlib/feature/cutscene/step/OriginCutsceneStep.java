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

public class OriginCutsceneStep extends CutsceneStep {
	public static final Codec<OriginCutsceneStep> CODEC = KVector.CODEC.xmap(OriginCutsceneStep::new, s -> s.position);

	public static final StreamCodec<RegistryFriendlyByteBuf, OriginCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		KVector.STREAM_CODEC, o -> o.position,
		OriginCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final ImBuilder<KVector> position = KVectorImBuilder.create(KVector.ZERO);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof OriginCutsceneStep s) {
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
			return new OriginCutsceneStep(position.build());
		}
	}

	public final KVector position;

	public OriginCutsceneStep(KVector position) {
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
		var v = position.get(ctx);

		if (v != null) {
			state.origin = v;
		}
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
