package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum ZoneToolItem implements VidLibTool {
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
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("lodestone");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (player.level().isClientSide() && !player.isShiftKeyDown()) {
			clickedOnZone(player);
		}

		if (player.level().isServerSide() && player.isShiftKeyDown()) {
			player.level().getEnvironment().removeZone(ID.idFromString("video:leviathan"), 0);
		}

		return true;
	}

	private void clickedOnZone(Player player) {
		var clip = Minecraft.getInstance().player.vl$sessionData().zoneClip;

		if (clip != null) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(clip, player.level(), player));
			player.c2s(new ZoneClickedPayload(clip.instance().container.id, clip.instance().index, clip.shape(), clip.distanceSq(), Optional.ofNullable(clip.pos())));
		}
	}
}
