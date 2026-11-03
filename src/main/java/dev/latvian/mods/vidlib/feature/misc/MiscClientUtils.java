package dev.latvian.mods.vidlib.feature.misc;

import com.google.common.hash.Hashing;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.blaze3d.platform.NativeImage;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiscClientUtils {
	public static final ConcurrentLinkedDeque<AutoCloseable> CLIENT_CLOSEABLE = new ConcurrentLinkedDeque<>();
	public static final ImBoolean PLAYER_HEADWEAR = new ImBoolean(true);
	public static final ImBoolean SPECTATE_UI = new ImBoolean(false);
	public static final Map<Identifier, NativeImage> BUILTIN_SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<Identifier, NativeImage> SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<UUID, PlayerModelType> UUID_MODEL_MAP = new HashMap<>();
	public static final Map<String, PlayerModelType> SKIN_MODEL_MAP = new HashMap<>();

	public static final Set<Item> NO_BOB_ITEMS = new ReferenceArraySet<>(Set.of(
		Items.COMPASS,
		Items.RECOVERY_COMPASS
	));

	public static void reloadShaders(Minecraft mc) {
		mc.reloadResourcePacks().thenRunAsync(() -> {
			mc.player.sendSystemMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN));
		}, mc);
	}

	public static int drawStackSize(GuiGraphicsExtractor graphics, Font font, String size, int x, int y, int color, boolean dropShadow) {
		var ms = graphics.pose();
		int w = font.width(size);
		float scale = size.length() >= 4 ? 0.5F : size.length() == 3 ? 0.75F : 1F;
		ms.pushMatrix();
		ms.translate((int) (x + 16F - (w - 1F) * scale), (int) (y + 16F - 7F * scale));
		ms.scale(scale, scale);
		int s = font.width(size);
		graphics.text(font, size, 0, 0, color, dropShadow);
		ms.popMatrix();
		return Mth.ceil(s * scale);
	}

	public static float adjustScreenX(Minecraft mc, int adjustedWidth) {
		return (mc.getWindow().getGuiScaledWidth() - adjustedWidth) / 2F;
	}

	public static int adjustScreenWidth(Minecraft mc, boolean shifted) {
		if (shifted) {
			float w = mc.getWindow().getGuiScaledWidth();
			float h = mc.getWindow().getGuiScaledHeight();
			// figure out how to shift to 16:9 nicely
		}

		return mc.getWindow().getGuiScaledWidth();
	}

	public static void setModel(UUID uuid, MinecraftProfileTextures textures, PlayerModelType model) {
		if (textures.skin() != null) {
			var hash = Hashing.sha1().hashUnencodedChars(textures.skin().getHash()).toString();
			UUID_MODEL_MAP.put(uuid, model);
			SKIN_MODEL_MAP.put(hash, model);
		}
	}

	public static void delayedExecute(Minecraft mc, Runnable task) {
		Thread.startVirtualThread(() -> mc.execute(task));
	}
}
