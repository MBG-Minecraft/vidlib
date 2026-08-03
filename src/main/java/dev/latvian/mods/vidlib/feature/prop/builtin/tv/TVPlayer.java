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

	private static URI playingUri;
	private static VideoPlayer videoPlayer;
	private static TextureWrapper textureWrapper;

	public static void start(URI uri) {
		start(uri, false, false, false);
	}

	public static void start(URI uri, boolean paused, boolean looping, boolean muted) {
		if (!uri.equals(playingUri)) {
			playingUri = uri;

			if (videoPlayer == null) {
				videoPlayer = new VideoPlayer(PlayerAPI.getFactory(), task -> Minecraft.getInstance().execute(() -> {
					task.run();
					GlStateManager._bindTexture(0);
				}));

				textureWrapper = new TextureWrapper(videoPlayer.texture(), videoPlayer.width(), videoPlayer.height());
				Minecraft.getInstance().getTextureManager().register(TEXTURE, textureWrapper);
			}

			videoPlayer.setMuteMode(muted);
			videoPlayer.setRepeatMode(looping);

			if (paused) {
				videoPlayer.startPaused(uri);
			} else {
				videoPlayer.start(uri);
			}
		} else {
			if (paused) {
				videoPlayer.pause();
				videoPlayer.seekTo(0L);
			} else {
				videoPlayer.seekTo(0L);
				videoPlayer.play();
			}
		}
	}

	public static void play() {
		if (videoPlayer != null) {
			videoPlayer.play();
		}
	}

	public static void pause() {
		if (videoPlayer != null) {
			videoPlayer.pause();
		}
	}

	public static void stop() {
		if (videoPlayer != null) {
			videoPlayer.stop();
			videoPlayer.release();
			videoPlayer = null;
			playingUri = null;
			textureWrapper = null;
		}
	}

	public static void reset() {
		if (videoPlayer != null) {
			videoPlayer.seekTo(0L);
		}
	}

	public static void mute() {
		if (videoPlayer != null) {
			videoPlayer.mute();
		}
	}

	public static void unmute() {
		if (videoPlayer != null) {
			videoPlayer.unmute();
		}
	}

	public static boolean hasVideo() {
		return videoPlayer != null && !videoPlayer.isStopped();
	}
}
