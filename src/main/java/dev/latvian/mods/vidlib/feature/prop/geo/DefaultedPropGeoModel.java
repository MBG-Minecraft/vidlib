package dev.latvian.mods.vidlib.feature.prop.geo;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class DefaultedPropGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
	private ResourceLocation modelPath;
	private ResourceLocation animationsPath;
	private ResourceLocation texturePath;

	public DefaultedPropGeoModel(ResourceLocation id, ResourceLocation texture) {
		this.modelPath = id.withPrefix("prop/");
		this.animationsPath = id.withPrefix("prop/");
		this.texturePath = texture;
	}

	public DefaultedPropGeoModel(ResourceLocation id) {
		this(id, id.withPath("textures/prop/" + id.getPath() + ".png"));
	}

	public DefaultedPropGeoModel<T> withModel(ResourceLocation id) {
		this.modelPath = id.withPrefix("prop/");
		return this;
	}

	public DefaultedPropGeoModel<T> withAnimations(ResourceLocation id) {
		this.animationsPath = id.withPrefix("prop/");
		return this;
	}

	public DefaultedPropGeoModel<T> withTexture(ResourceLocation texture) {
		this.texturePath = texture;
		return this;
	}

	@Override
	public ResourceLocation getModelResource(GeoRenderState renderState) {
		return modelPath;
	}

	@Override
	public ResourceLocation getTextureResource(GeoRenderState renderState) {
		return texturePath;
	}

	@Override
	public ResourceLocation getAnimationResource(T animatable) {
		return animationsPath;
	}
}
