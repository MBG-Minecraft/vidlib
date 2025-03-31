package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class TrackedDataMapValue {
	public final DataType<?> type;
	public Object data = null;
	public int save = -1;
	public int sync = -1;
	public int changeCount = 0;

	public TrackedDataMapValue(DataType<?> type) {
		this.type = type;
	}

	public void setChanged() {
		if (changeCount == Integer.MAX_VALUE) {
			changeCount = 0;
		} else {
			changeCount++;
		}
	}

	public void update(@Nullable Player player, Object update) {
		data = update;

		if (player != null && type.onReceived() != null) {
			type.onReceived().accept(player, Cast.to(data));
		}
	}
}
