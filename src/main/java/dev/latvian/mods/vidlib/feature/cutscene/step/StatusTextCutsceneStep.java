package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class StatusTextCutsceneStep extends CutsceneStep {
	public static final Codec<StatusTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(StatusTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, StatusTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, o -> o.text,
		StatusTextCutsceneStep::new
	);

	public final Component text;

	public StatusTextCutsceneStep(Component text) {
		this.text = text;
	}

	public StatusTextCutsceneStep() {
		this.text = Component.empty();
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.STATUS_TEXT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var mc = Minecraft.getInstance();

		if (mc.player != null) {
			mc.player.sendOverlayMessage(text);
		}
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (snap) {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.sendOverlayMessage(Component.empty());
			}
		}
	}
}
