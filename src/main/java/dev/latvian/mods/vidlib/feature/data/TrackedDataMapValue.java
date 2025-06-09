package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class TrackedDataMapValue {
	public final DataKey<?> key;
	public Object data = null;
	public int save = -1;
	public int sync = -1;
	public int changeCount = 0;

	public TrackedDataMapValue(DataKey<?> key) {
		this.key = key;
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

		if (player != null && key.onReceived() != null) {
			key.onReceived().accept(player, Cast.to(data));
		}
	}
}
