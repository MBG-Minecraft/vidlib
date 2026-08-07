package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.opengl.GlStateManager;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;

import java.net.URI;

public class TVPlayer {
	public static final ResourceLocation TEXTURE = VidLib.id("textures/prop/tv/video.png");

	private static URI current;
	private static VideoPlayer player;
	private static TextureWrapper textureWrapper;

	public static void start(URI uri) {
		start(uri, false, false, false);
	}

	public static void start(URI uri, boolean paused, boolean looping, boolean muted) {
		if (!uri.equals(current)) {
			current = uri;

			if (player == null) {
				player = new VideoPlayer(PlayerAPI.getFactory(), task -> Minecraft.getInstance().execute(() -> {
					task.run();
					GlStateManager._bindTexture(0);
				}));

				textureWrapper = new TextureWrapper(player);
				Minecraft.getInstance().getTextureManager().register(TEXTURE, textureWrapper);
			}

			player.setMuteMode(muted);
			player.setRepeatMode(looping);

			if (paused) {
				player.startPaused(uri);
			} else {
				player.start(uri);
			}
		} else {
			if (paused) {
				player.pause();
				player.seekTo(0L);
			} else {
				player.seekTo(0L);
				player.play();
			}
		}
	}

	public static void play() {
		if (player != null) {
			player.play();
		}
	}

	public static void pause() {
		if (player != null) {
			player.pause();
		}
	}

	public static void stop() {
		if (player != null) {
			player.stop();
			player.release();
			player = null;
			current = null;
			textureWrapper = null;
		}
	}

	public static void reset() {
		if (player != null) {
			player.seekTo(0L);
		}
	}

	public static void mute() {
		if (player != null) {
			player.mute();
		}
	}

	public static void unmute() {
		if (player != null) {
			player.unmute();
		}
	}

	public static boolean hasVideo() {
		return player != null && !player.isStopped();
	}
}
