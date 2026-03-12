package dev.latvian.mods.vidlib.integration.replay;

import com.google.gson.JsonObject;
import dev.latvian.mods.replay.api.ReplaySessionData;
import dev.latvian.mods.replay.api.ReplaySessionDataType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.prop.PropListType;

public class SelectedPropReplaySessionData implements ReplaySessionData {
	public static final ReplaySessionDataType<SelectedPropReplaySessionData> TYPE = new ReplaySessionDataType<>(VidLib.id("selected_prop"), SelectedPropReplaySessionData::new);

	public int selectedProp = 0;
	public JsonObject selectedPropData = null;
	public PropListType selectedPropList = PropListType.LEVEL;
	public boolean openSelectedPropPopup = false;

	@Override
	public ReplaySessionDataType<?> getType() {
		return TYPE;
	}
}
