package dev.latvian.mods.vidlib.feature.prop.geo;

import dev.latvian.mods.vidlib.feature.prop.Prop;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;

public interface GeoProp extends GeoAnimatable {
	@ApiStatus.NonExtendable
	@Nullable
	default <D> D getAnimData(SerializableDataTicket<D> dataTicket) {
		return getAnimatableInstanceCache().getManagerForId(((Prop) this).id).getAnimatableData(dataTicket);
	}

	@ApiStatus.NonExtendable
	default <D> void setAnimData(SerializableDataTicket<D> dataTicket, D data) {
		var prop = (Prop) this;

		if (prop.level.isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(prop.id).setAnimatableData(dataTicket, data);
		} else {
			throw new IllegalStateException("Currently not supported on server side");
			// GeckoLibServices.NETWORK.syncEntityAnimData(prop, false, dataTicket, data);
		}
	}

	@ApiStatus.NonExtendable
	default void triggerAnim(@Nullable String controllerName, String animName) {
		var prop = (Prop) this;

		if (prop.level.isClientSide()) {
			var animatableManager = getAnimatableInstanceCache().getManagerForId(prop.id);

			if (animatableManager == null) {
				return;
			}

			if (controllerName != null) {
				animatableManager.tryTriggerAnimation(controllerName, animName);
			} else {
				animatableManager.tryTriggerAnimation(animName);
			}
		} else {
			throw new IllegalStateException("Currently not supported on server side");
			// GeckoLibServices.NETWORK.triggerEntityAnim(prop, false, controllerName, animName);
		}
	}

	@ApiStatus.NonExtendable
	default void stopTriggeredAnim(@Nullable String controllerName, @Nullable String animName) {
		var prop = (Prop) this;

		if (prop.level.isClientSide()) {
			var animatableManager = getAnimatableInstanceCache().getManagerForId(prop.id);

			if (animatableManager == null) {
				return;
			}

			if (controllerName != null) {
				animatableManager.stopTriggeredAnimation(controllerName, animName);
			} else {
				animatableManager.stopTriggeredAnimation(animName);
			}
		} else {
			throw new IllegalStateException("Currently not supported on server side");
			// GeckoLibServices.NETWORK.stopTriggeredEntityAnim(prop, false, controllerName, animName);
		}
	}

	@Override
	default double getTick(@Nullable Object entity) {
		return ((Prop) this).tick;
	}
}
