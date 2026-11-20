package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ScreenEffectClientInfo<SE extends ScreenEffect> {
	@Nullable
	public static ScreenEffectClientInfo<?> get(DynamicType<?, ?> type) {
		return type.getDynamicTypeProperty(ScreenEffect.PROPERTY_EXTRACTOR, ScreenEffectClientInfo.class);
	}

	public final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type;
	public ImIcon icon;
	public String displayName;
	public Supplier<? extends ScreenEffectState> stateFactory;
	public ScreenEffectStateExtractor<SE, ? extends ScreenEffectState> stateExtractor;

	public ScreenEffectClientInfo(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type) {
		this.type = type;
		this.icon = ImIcons.PALETTE;
		this.displayName = type.key();
	}

	public void setDisplayInfo(ImIcon icon, String displayName) {
		this.icon = icon;
		this.displayName = displayName;
	}

	public <S extends ScreenEffectState> void setState(Supplier<S> stateFactory, ScreenEffectStateExtractor<SE, S> stateExtractor) {
		this.stateFactory = stateFactory;
		this.stateExtractor = stateExtractor;
	}

	/*
	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(ID.vidlib("screen_effects"), builder -> {
		builder.setTickCallback(ScreenEffectRenderer::tick);
		builder.setDrawSetupCallback(ScreenEffectRenderer::setup);
		builder.addUniform(CanvasUniform.int1("Count", () -> uCount));
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
	});

	private static void tick(Minecraft mc) {
		if (!mc.getPauseType().tick()) {
			return;
		}

		var ctx = mc.level.getGlobalContext();
		var effects = mc.player.vl$sessionData().screenEffects;

		if (!effects.isEmpty()) {
			effects.removeIf(effect -> {
				effect.snap();
				var ectx = ctx.fork(effect.variables);
				float progress = effect.duration <= 1 ? 0F : (effect.tick / (float) effect.duration);
				ectx.progress = (double) progress;
				effect.update(ectx);

				if (!effect.paused) {
					effect.tick++;
				}

				return !effect.paused && effect.duration > 0 && effect.tick >= effect.duration;
			});
		}
	}

	private static void setup(Minecraft mc) {
		var effects = mc.player.vl$sessionData().screenEffects;
		float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		if (!effects.isEmpty()) {
			var texture = ScreenEffectTexture.HOLDER.texture().get();
			uCount = texture.update(effects, delta);
			// FIXME CANVAS.markActive();
		} else {
			uCount = 0;
		}
	}
	 */
}
