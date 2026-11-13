package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
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
		var hit = ClientSessionData.of(Minecraft.getInstance()).zoneHit;

		if (hit != null) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(player, hit));
			PlayerTarget.of(player).c2s(new ZoneClickedPayload(hit.zone(), hit.pos(), hit.distanceSq(), Optional.ofNullable(hit.direction()), hit.blockPos()));
		}
	}
}
