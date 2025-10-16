package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class StatusTextCutsceneStep extends CutsceneStep {
	public static final Codec<StatusTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(StatusTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, StatusTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, o -> o.text,
		StatusTextCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final TextComponentImBuilder text = new TextComponentImBuilder(true);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof StatusTextCutsceneStep s) {
				text.set(s.text);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return text.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return text.isValid();
		}

		@Override
		public CutsceneStep build() {
			return new StatusTextCutsceneStep(text.build());
		}
	}

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
			mc.player.status(text);
		}
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (snap) {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.status(Component.empty());
			}
		}
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
