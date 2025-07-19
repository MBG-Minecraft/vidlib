package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.HUDNameEvent;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;

public interface VLGuiGraphics {
	ResourceLocation HEART_BACKGROUND = ID.mc("hud/heart/container");
	ResourceLocation FULL_HEART = ID.mc("hud/heart/full");

	default Minecraft vl$mc() {
		return Minecraft.getInstance();
	}

	default void healthBar(int x, int y, int w, int h, float hp, int alpha) {
		if (alpha <= 3) {
			return;
		}

		var graphics = (GuiGraphics) this;
		graphics.fill(x, y, x + w, y + h, 0xFFFFFF | (alpha << 24));
		graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, (alpha << 24));
		var barColor = Color.hsb(KMath.lerp(hp, 0F, 0.35F), 0.71F, 1F, alpha);
		graphics.fill(x + 2, y + 2, x + Mth.ceil((w - 3F) * hp + 1F), y + h - 2, barColor.argb());
	}

	default void healthBarWithLabel(Font font, Entity entity, int x, int y, int w, int h, boolean renderName, boolean renderHealth, int alpha) {
		var graphics = (GuiGraphics) this;
		var mc = vl$mc();
		var hp = entity.getRelativeHealth(mc.getDeltaTracker().getGameTimeDeltaPartialTick(entity == mc.player));
		var nameY = y;

		if (renderHealth) {
			healthBar(x, y, w, h, hp, alpha);
		} else {
			nameY += 6;
		}

		if (!renderName) {
			return;
		}

		var lines = new ArrayList<FormattedCharSequence>();
		lines.addAll(font.split(entity.getDisplayName(), 1000));

		if (entity instanceof Player player) {
			Scoreboard scoreboard = player.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
			if (objective != null) {
				ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(player, objective);
				Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
				lines.addAll(font.split(Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName()), 1000));
			}
		}

		NeoForge.EVENT_BUS.post(new HUDNameEvent(entity, font, lines));

		graphics.pose().pushPose();
		graphics.pose().translate(x + w / 2F, nameY - 6F, 0F);

		for (int i = 0; i < lines.size(); i++) {
			var line = lines.get(i);
			graphics.drawString(font, line, -font.width(line) / 2F, -3F - (lines.size() - 1) * 9 + i * 9, 0xFFFFFF | (alpha << 24), true);
		}

		graphics.pose().popPose();
	}

	default void healthBarWithSideLabel(Font font, LivingEntity entity, int x, int y, int w, int h, boolean leftSide, boolean renderName, boolean renderHealth) {
		var graphics = (GuiGraphics) this;
		var health = entity.getHealth();
		var maxHealth = entity.getMaxHealth();

		if (entity instanceof Player) {
			health /= 2F;
			maxHealth /= 2F;
		}

		var hp = health / maxHealth;

		if (renderHealth) {
			healthBar(x, y, w, h, hp, 255);
		}

		if (!renderName) {
			return;
		}

		var name = font.split(entity.getDisplayName(), 1000).getFirst();

		var str = KMath.format(health) + " / " + KMath.format(maxHealth);
		graphics.drawString(font, str, x + (w - font.width(str)) / 2, y + 3, 0xFFFFFFFF, true);

		if (leftSide) {
			graphics.drawString(font, name, x + w + 4, y + 3, 0xFFFFFFFF, true);
		} else {
			graphics.drawString(font, name, x - 4 - font.width(name), y + 3, 0xFFFFFFFF, true);
		}
	}

	default void heart(int x, int y, float value) {
		var graphics = (GuiGraphics) this;
		graphics.blitSprite(VidLibRenderTypes.GUI, HEART_BACKGROUND, 9, 9, 0, 0, x, y, 9, 9);
		graphics.blitSprite(VidLibRenderTypes.GUI, FULL_HEART, 9, 9, 0, 0, x, y, Mth.ceil(9F * value), 9);
	}
}
