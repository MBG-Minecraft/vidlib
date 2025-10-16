package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.CustomCutsceneEvent;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class CustomEventCutsceneStep extends CutsceneStep {
	public static final Codec<CustomEventCutsceneStep> CODEC = Codec.STRING.xmap(CustomEventCutsceneStep::new, s -> s.event);

	public static final StreamCodec<RegistryFriendlyByteBuf, CustomEventCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, o -> o.event,
		CustomEventCutsceneStep::new
	);

	public static class Builder extends CutsceneStepImBuilder {
		public final ImString event = ImGuiUtils.resizableString();

		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof CustomEventCutsceneStep s) {
				event.set(s.event);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.text("Event");
			ImGui.inputText("###event", event);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return event.isNotEmpty();
		}

		@Override
		public CutsceneStep build() {
			return new CustomEventCutsceneStep(event.get());
		}
	}

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

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
