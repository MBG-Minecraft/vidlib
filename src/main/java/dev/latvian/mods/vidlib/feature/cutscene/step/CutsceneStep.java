package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class CutsceneStep {
	public static final Codec<CutsceneStep> CODEC = CutsceneStepType.DATA_TYPE.codec().dispatch("type", CutsceneStep::type, t -> t.mapCodec);
	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> STREAM_CODEC = CutsceneStepType.STREAM_CODEC.dispatch(CutsceneStep::type, t -> t.streamCodec);

	public int start = 0;
	public int length = 20;
	public boolean snap = true;

	public abstract CutsceneStepType type();

	public int getFlags() {
		return snap ? 1 : 0;
	}

	public void setFlags(int flags) {
		snap = (flags & 1) != 0;
	}

	public void start(CutsceneState state, KNumberContext ctx) {
	}

	public abstract void tick(CutsceneState state, KNumberContext ctx);

	public void exit(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);
	}

	public abstract CutsceneStepImBuilder createBuilder();
}
