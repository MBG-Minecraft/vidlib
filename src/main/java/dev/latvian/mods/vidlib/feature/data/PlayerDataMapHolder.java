package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface PlayerDataMapHolder extends DataMapHolder {
	default void setSuspended(boolean value) {
		set(InternalPlayerData.SUSPENDED, value);
	}

	default void setNickname(Component nickname) {
		set(InternalPlayerData.NICKNAME, Empty.isEmpty(nickname) ? Empty.COMPONENT : nickname);
	}

	default void setPlumbob(Icon icon) {
		set(InternalPlayerData.PLUMBOB, icon.holder());
	}

	default void setClothing(Clothing clothing) {
		set(InternalPlayerData.CLOTHING, clothing);
	}

	default void setSkinOverride(@Nullable SkinTexture skin) {
		set(InternalPlayerData.SKIN_OVERRIDE, skin);
	}

	default void setCapeOverride(@Nullable ResourceLocation cape) {
		set(InternalPlayerData.CAPE_OVERRIDE, cape);
	}

	default void setElytraOverride(@Nullable ResourceLocation cape) {
		set(InternalPlayerData.ELYTRA_OVERRIDE, cape);
	}

	default void setFlightSpeedMod(float value) {
		set(InternalPlayerData.FLIGHT_SPEED, value);
	}
}
