package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.core.VLParticleEngine;
import dev.latvian.mods.vidlib.feature.particle.CustomParticleGroup;
import dev.latvian.mods.vidlib.feature.particle.SortedQuadParticleGroup;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticleRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 26.3 port of VidLib's {@code ParticleEngine} hooks.
 * <p>
 * The engine no longer renders anything: it only
 * {@link ParticleEngine#extract(net.minecraft.client.renderer.state.level.ParticlesRenderState, Frustum, Camera, float)}
 * per-frame state. Two hooks are therefore needed:
 * <ul>
 * <li> extending {@code RENDER_ORDER} so VidLib's own particle groups get extracted at all; </li>
 * <li> creating VidLib's {@link ParticleGroup}s (distance-sorted quads + the custom-geometry group)
 *      when a particle with one of those render types is queued. </li>
 * </ul>
 * The 1.21.5 sprite-set mixins (which allowed particle JSONs for unregistered particle types via
 * {@code ParticleEngine.loadParticleDescription}) were dropped: that method no longer exists on
 * {@code ParticleEngine} - it lives on {@code ParticleResources} in 26.3 - and all four of VidLib's
 * particle JSONs ({@code burn_smoke, spark, wind, fire}) already have registered particle types, so
 * vanilla's own sprite-set loading covers them.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements VLParticleEngine {
	@Shadow
	protected ClientLevel level;

	@Shadow
	@Final
	private ParticleResources resourceManager;

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
	private <K, V> V vl$createGroup(Map<K, V> instance, K key, Function<? super K, ? extends V> factory) {
		if (key instanceof ParticleRenderType type && !VidLibParticleRenderTypes.isVanillaType(type)) {
			return Cast.to(createVidLibParticleGroup(type));
		}

		return instance.computeIfAbsent(key, factory);
	}

	@Unique
	private ParticleGroup<?> createVidLibParticleGroup(ParticleRenderType type) {
		if (type == VidLibParticleRenderTypes.CUSTOM) {
			return new CustomParticleGroup(Cast.to(this), type);
		}

		return new SortedQuadParticleGroup(Cast.to(this), type);
	}

	@ModifyExpressionValue(method = "extract", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;particleRenderOrder:Ljava/util/List;"))
	private List<ParticleRenderType> vl$renderOrder(List<ParticleRenderType> original) {
		return VidLibParticleRenderTypes.renderOrder();
	}

	@Override
	public TextureAtlas getTextureAtlas() {
		return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
	}

	@Override
	@Nullable
	public SpriteSet getSpriteSet(Identifier id) {
		try {
			Field field = ParticleResources.class.getDeclaredField("spriteSets");
			field.setAccessible(true);
			return Cast.to(((Map<?, ?>) field.get(resourceManager)).get(id));
		} catch (Throwable ignored) {
			return null;
		}
	}
}
