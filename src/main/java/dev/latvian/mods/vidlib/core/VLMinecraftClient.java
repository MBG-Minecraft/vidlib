package dev.latvian.mods.vidlib.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.UpdateGlobalDataValuePayload;
import dev.latvian.mods.vidlib.feature.data.UpdateLevelDataValuePayload;
import dev.latvian.mods.vidlib.feature.data.UpdatePlayerDataValuePayload;
import dev.latvian.mods.vidlib.feature.misc.MainMenuOpenedEvent;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("resource")
public interface VLMinecraftClient extends VLMinecraftEnvironment {
	Cache<String, SystemToast.SystemToastId> SYSTEM_TOAST_IDS = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	default Minecraft vl$self() {
		return (Minecraft) this;
	}

	@Override
	default boolean isClientEnvironment() {
		return true;
	}

	default ClientSessionData vl$getGlobalData() {
		throw new NoMixinException(this);
	}

	default void vl$setGlobalData(@Nullable ClientSessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default ClientLevel vl$level() {
		return vl$self().level;
	}

	default float linearizeDepth(float depth) {
		float near = 0.05F;
		float far = vl$self().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar;
		return near * far / (far + depth * (near - far));
	}

	default TextureAtlasSprite getSprite(SpriteKey sprite) {
		return vl$self().getAtlasManager().get(new SpriteId(sprite.atlas(), sprite.sprite()));
	}

	default UV getSpriteUV(SpriteKey sprite) {
		return sprite.isSpecial() ? UV.FULL : getSprite(sprite).uv();
	}

	default <T> void updateServerDataValue(DataKey<GlobalSessionData, T> key, T value) {
		vl$self().player.c2s(new UpdateGlobalDataValuePayload(List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updateLevelDataValue(ResourceKey<Level> dimension, DataKey<LevelSessionData<?>, T> key, T value) {
		vl$self().player.c2s(new UpdateLevelDataValuePayload(dimension, List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updatePlayerDataValue(UUID player, DataKey<PlayerSessionData, T> key, T value) {
		vl$self().player.c2s(new UpdatePlayerDataValuePayload(player, List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updatePlayerDataValue(DataKey<PlayerSessionData, T> type, T value) {
		updatePlayerDataValue(vl$self().player.getUUID(), type, value);
	}

	default void runClientCommand(String command) {
		vl$self().player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
	}

	@Override
	@Nullable
	default String getServerBrand() {
		var p = vl$self().player;
		return p == null ? null : p.connection.serverBrand();
	}

	default float getEffectScale() {
		return vl$self().getWindow().getGuiScaledHeight() / 1080F;
	}

	default int vl$reloadCount() {
		return 0;
	}

	default void vl$exitToTitle() {
		var mc = vl$self();

		if (mc.level != null) {
			mc.level.disconnect(Component.translatable("menu.savingLevel"));

			if (mc.isLocalServer()) {
				mc.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")), true);
			} else {
				mc.disconnect(new TitleScreen(), false);
			}
		}

		mc.gui.setScreen(new TitleScreen());
		NeoForge.EVENT_BUS.post(new MainMenuOpenedEvent(mc, false));
	}
}
