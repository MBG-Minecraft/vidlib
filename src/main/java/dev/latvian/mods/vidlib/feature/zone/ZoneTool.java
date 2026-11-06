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
		var hit = Minecraft.getInstance().player.vl$sessionData().zoneHit;

		if (hit != null) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(hit, player.level(), player));
			player.c2s(new ZoneClickedPayload(hit.resolvedZone().ref, hit.resolvedZone().index, hit.pos(), hit.distanceSq(), Optional.ofNullable(hit.direction())));
		}
	}
}
