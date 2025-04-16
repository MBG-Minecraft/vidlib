package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ZoneToolItem implements VidLibTool {
	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new ZoneToolItem());
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
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (player.level().isClientSide()) {
			clickedOnZone(player);
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
