package dev.latvian.mods.vidlib.integration.replay;

import dev.latvian.mods.replay.api.ReplaySessionData;
import dev.latvian.mods.replay.api.ReplaySessionDataType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropListType;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashSet;

public class SelectedPropReplaySessionData implements ReplaySessionData {
	public static final ReplaySessionDataType<SelectedPropReplaySessionData> TYPE = new ReplaySessionDataType<>(VidLib.id("selected_prop"), SelectedPropReplaySessionData::new);

	public int selectedProp = 0;
	public Tag selectedPropData = null;
	public PropListType selectedPropList = PropListType.LEVEL;
	public boolean openSelectedPropPopup = false;
	public LinkedHashSet<Prop> makePropKeyframes = new LinkedHashSet<>();

	@Override
	public ReplaySessionDataType<?> getType() {
		return TYPE;
	}
}
