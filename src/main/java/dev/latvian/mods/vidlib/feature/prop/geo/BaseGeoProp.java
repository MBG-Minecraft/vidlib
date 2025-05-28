package dev.latvian.mods.vidlib.feature.prop.geo;

import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.util.Lazy;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BaseGeoProp extends Prop implements GeoProp {
	private final Lazy<AnimatableInstanceCache> geoCache = Lazy.of(() -> GeckoLibUtil.createInstanceCache(this, false));

	public BaseGeoProp(PropContext<?> ctx) {
		super(ctx);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache.get();
	}
}
