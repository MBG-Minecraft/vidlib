package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class BottomBarTextCutsceneStep extends CutsceneStep {
	public static final Codec<BottomBarTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(BottomBarTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, BottomBarTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, s -> s.text,
		BottomBarTextCutsceneStep::new
	);

	public final Component text;

	public BottomBarTextCutsceneStep(Component text) {
		this.text = text;
	}

	public BottomBarTextCutsceneStep() {
		this.text = Component.empty();
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.BOTTOM_BAR_TEXT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		state.bottomBar.add(text);
	}
}
