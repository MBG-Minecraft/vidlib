package dev.beast.mods.shimmer.feature.data;

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
}
