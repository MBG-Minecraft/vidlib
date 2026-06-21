package dev.latvian.mods.vidlib.feature.prop.geo;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;

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
