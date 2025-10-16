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

public class TopBarTextCutsceneStep extends CutsceneStep {
	public static final Codec<TopBarTextCutsceneStep> CODEC = ComponentSerialization.CODEC.xmap(TopBarTextCutsceneStep::new, s -> s.text);

	public static final StreamCodec<RegistryFriendlyByteBuf, TopBarTextCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, s -> s.text,
		TopBarTextCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final TextComponentImBuilder text = new TextComponentImBuilder(true);

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof TopBarTextCutsceneStep s) {
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
			return new TopBarTextCutsceneStep(text.build());
		}
	}

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

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
