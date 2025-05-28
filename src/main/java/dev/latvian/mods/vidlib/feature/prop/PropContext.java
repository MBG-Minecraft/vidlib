package dev.latvian.mods.vidlib.feature.prop;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record PropContext<P extends Prop>(PropList<?> propList, PropType<P> type, PropSpawnType spawnType, @Nullable CompoundTag initialData) {
	public PropContext(PropList<?> propList, PropType<P> type) {
		this(propList, type, PropSpawnType.GAME, null);
	}
}
