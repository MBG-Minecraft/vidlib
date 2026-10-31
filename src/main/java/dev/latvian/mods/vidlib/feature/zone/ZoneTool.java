package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum ZoneTool implements VidLibTool {
	@AutoRegister
	INSTANCE;

	@Override
	public String getId() {
		return "zone";
	}

	@Override
	public Component getName() {
		return Component.literal("Zone Tool");
	}

	@Override
	public Identifier getModel() {
		return Identifier.withDefaultNamespace("lodestone");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (player.level().isClientSide() && !player.isShiftKeyDown()) {
			clickedOnZone(player);
		}

		return true;
	}

	private void clickedOnZone(Player player) {
		var clip = Minecraft.getInstance().player.vl$sessionData().zoneClip;

		if (clip != null) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(clip, player.level(), player));
			player.c2s(new ZoneClickedPayload(clip.instance().container.ref, clip.instance().index, clip.distanceSq(), Optional.ofNullable(clip.pos())));
		}
	}
}
