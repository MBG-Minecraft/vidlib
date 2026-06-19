package dev.latvian.mods.vidlib.feature.prop.geo;

import net.minecraft.resources.Identifier;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class DefaultedPropGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
	private Identifier modelPath;
	private Identifier animationsPath;
	private Identifier texturePath;

	public DefaultedPropGeoModel(Identifier id, Identifier texture) {
		this.modelPath = id.withPrefix("prop/");
		this.animationsPath = id.withPrefix("prop/");
		this.texturePath = texture;
	}

	public DefaultedPropGeoModel(Identifier id) {
		this(id, id.withPath("textures/prop/" + id.getPath() + ".png"));
	}

	public DefaultedPropGeoModel<T> withModel(Identifier id) {
		this.modelPath = id.withPrefix("prop/");
		return this;
	}

	public DefaultedPropGeoModel<T> withAnimations(Identifier id) {
		this.animationsPath = id.withPrefix("prop/");
		return this;
	}

	public DefaultedPropGeoModel<T> withTexture(Identifier texture) {
		this.texturePath = texture;
		return this;
	}

	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return modelPath;
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		return texturePath;
	}

	@Override
	public Identifier getAnimationResource(T animatable) {
		return animationsPath;
	}
}
