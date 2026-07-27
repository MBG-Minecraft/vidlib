package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface PlayerDataMapHolder extends DataMapHolder {
	default void setSuspended(boolean value) {
		set(InternalPlayerData.SUSPENDED, value);
	}

	default void setNickname(Component nickname) {
		set(InternalPlayerData.NICKNAME, Empty.isEmpty(nickname) ? Empty.COMPONENT : nickname);
	}

	default void setPlumbob(Ref<Icon> icon) {
		set(InternalPlayerData.PLUMBOB, icon);
	}

	default void setClothing(PlayerClothing clothing) {
		set(InternalPlayerData.CLOTHING, clothing);
	}

	default void setSkinOverride(@Nullable SkinTexture skin) {
		set(InternalPlayerData.SKIN_OVERRIDE, skin);
	}

	default void setCapeOverride(@Nullable ClientAsset.ResourceTexture cape) {
		set(InternalPlayerData.CAPE_OVERRIDE, cape);
	}

	default void setElytraOverride(@Nullable ClientAsset.ResourceTexture cape) {
		set(InternalPlayerData.ELYTRA_OVERRIDE, cape);
	}

	default void setFlightSpeedMod(float value) {
		set(InternalPlayerData.FLIGHT_SPEED, value);
	}
}
