package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public class ZoneToolItem implements ShimmerTool {
	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new ZoneToolItem());
	}

	@Override
	public String getId() {
		return "zone";
	}

	@Override
	public Component getName() {
		return Component.literal("Zone Tool");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.LODESTONE);
	}

	@Override
	public boolean use(Player player, ItemStack item) {
		if (player.level().isClientSide()) {
			clickedOnZone(player);
		}

		return true;
	}

	private void clickedOnZone(Player player) {
		var clip = Minecraft.getInstance().player.shimmer$sessionData().zoneClip;

		if (clip != null) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.ClickedOn(clip, player.level(), player));
			player.c2s(new ZoneClickedPayload(clip.instance().container.id, clip.instance().index, clip.shape(), clip.distanceSq(), Optional.ofNullable(clip.pos())));
		}
	}
}
