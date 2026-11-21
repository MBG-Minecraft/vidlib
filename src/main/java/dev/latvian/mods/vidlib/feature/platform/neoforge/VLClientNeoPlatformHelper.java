package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;

public class VLClientNeoPlatformHelper extends VLNeoPlatformHelper {
	public VLClientNeoPlatformHelper(ModContainer mod) {
		super(mod);
	}

	@Override
	public LevelSessionData<?> getClientSessionData(Level level) {
		return ClientLevelSessionData.of(level);
	}
}
