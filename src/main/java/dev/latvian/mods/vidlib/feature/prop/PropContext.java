package dev.latvian.mods.vidlib.feature.prop;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record PropContext<P extends Prop>(Props<?> props, PropType<P> type, PropSpawnType spawnType, long createdTime, @Nullable CompoundTag initialData) {
}
