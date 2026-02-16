package dev.latvian.mods.vidlib.feature.item;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.color.Color;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RainbowItemTint implements ItemTintSource {
	public static final RainbowItemTint INSTANCE = new RainbowItemTint();
	public static final MapCodec<RainbowItemTint> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
		if (level != null) {
			return Color.hsb((level.getGameTime() % 24000L) / 240F, 1F, 1F, 255).argb();
		}

		return 0xFFFF0000;
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return MAP_CODEC;
	}
}
