package dev.beast.mods.shimmer.feature.explosion;

import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.core.ShimmerItem;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.config.ConfigScreen;
import dev.beast.mods.shimmer.feature.item.ItemScreen;
import dev.beast.mods.shimmer.feature.item.UpdateItemDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExplosionTestToolScreen extends ConfigScreen<ExplosionData> {
	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	public static void registerScreen() {
		ItemScreen.TOOLS.put("explosion_test", (player, stack, hand) -> new ExplosionTestToolScreen(player.level(), stack, hand));
	}

	private ExplosionTestToolScreen(Level level, ItemStack stack, InteractionHand hand) {
		super(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), ExplosionTestTool.getData(stack, true), ExplosionData.DEFAULT, ExplosionData.CONFIG, data -> {
			var mc = Minecraft.getInstance();
			var tag = new CompoundTag();
			tag.put("explosion_data", ExplosionData.CODEC.encodeStart(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), data).getOrThrow());
			ShimmerItem.partiallyMergeCustomData(stack, tag);
			mc.c2s(new UpdateItemDataPayload(hand, tag));
		});
	}

	@Override
	protected void init() {
		super.init();
		addCopyJsonButton(ExplosionData.CODEC);
	}
}
