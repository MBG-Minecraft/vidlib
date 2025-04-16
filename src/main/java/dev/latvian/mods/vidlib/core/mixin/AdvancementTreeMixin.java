package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;

@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public void addAll(Collection<AdvancementHolder> advancements) {
	}
}
