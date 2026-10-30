package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class TopBarTextCutsceneStep extends CutsceneStep {
	public static final Codec<TopBarTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(TopBarTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, TopBarTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, s -> s.text,
		TopBarTextCutsceneStep::new
	);

	public final Component text;

	public TopBarTextCutsceneStep(Component text) {
		this.text = text;
	}

	public TopBarTextCutsceneStep() {
		this.text = Component.empty();
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.TOP_BAR_TEXT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		state.topBar.add(text);
	}
}
