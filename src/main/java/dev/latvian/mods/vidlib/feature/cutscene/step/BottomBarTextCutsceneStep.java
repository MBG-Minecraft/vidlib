package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class BottomBarTextCutsceneStep extends CutsceneStep {
	public static final Codec<BottomBarTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(BottomBarTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, BottomBarTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, s -> s.text,
		BottomBarTextCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final TextComponentImBuilder text = new TextComponentImBuilder(true);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof BottomBarTextCutsceneStep s) {
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
			return new BottomBarTextCutsceneStep(text.build());
		}
	}

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

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
