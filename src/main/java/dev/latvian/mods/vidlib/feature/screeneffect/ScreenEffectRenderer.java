package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.client.CustomPostPassUniforms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ScreenEffectRenderer {
	@Nullable
	public static ScreenEffectRenderer get(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type) {
		return type.getProperty(ScreenEffect.PROPERTY_RENDERER) instanceof ScreenEffectRenderer r ? r : null;
	}

	public static void applyEffect(List<Identifier> effects) {
		for (var type : ScreenEffect.REGISTRY.typeList()) {
			if (type instanceof DynamicType<?, ?> t && t.getProperty(ScreenEffect.PROPERTY_RENDERER) instanceof ScreenEffectRenderer r) {
				if (r.postEffect != null && r.postEffectEnabled.getAsBoolean()) {
					effects.add(r.postEffect);
				}
			}
		}
	}

	public final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type;
	public final List<CustomPostPassUniforms> uniforms;
	public Identifier postEffect;
	public BooleanSupplier postEffectEnabled;

	public ScreenEffectRenderer(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type) {
		this.type = type;
		this.uniforms = new ArrayList<>(1);
		this.postEffect = null;
		this.postEffectEnabled = () -> true;
	}

	public void addUniforms(CustomPostPassUniforms uniforms) {
		this.uniforms.add(uniforms);
	}

	public void setPostEffect(Identifier id, BooleanSupplier enabled) {
		this.postEffect = id;
		this.postEffectEnabled = enabled;
	}

	public void update() {
		for (var uniform : uniforms) {
			uniform.update();
		}
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
