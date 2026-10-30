package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.cutscene.CustomCutsceneEvent;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.NeoForge;

public class CustomEventCutsceneStep extends CutsceneStep {
	public static final Codec<CustomEventCutsceneStep> CODEC = Codec.STRING.xmap(CustomEventCutsceneStep::new, s -> s.event);

	public static final StreamCodec<RegistryFriendlyByteBuf, CustomEventCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, o -> o.event,
		CustomEventCutsceneStep::new
	);

	public final String event;

	public CustomEventCutsceneStep(String event) {
		this.event = event;
	}

	public CustomEventCutsceneStep() {
		this.event = "";
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.CUSTOM_EVENT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		if (!event.isEmpty()) {
			NeoForge.EVENT_BUS.post(new CustomCutsceneEvent.Tick(ctx, event));
		}
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (!event.isEmpty()) {
			NeoForge.EVENT_BUS.post(new CustomCutsceneEvent.Exit(ctx, event));
		}
	}
}
