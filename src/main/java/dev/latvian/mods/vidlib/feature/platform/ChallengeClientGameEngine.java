package dev.latvian.mods.vidlib.feature.platform;

import net.minecraft.world.entity.LightningBolt;

public class ChallengeClientGameEngine extends ClientGameEngine {
	@Override
	public boolean disableLightningSounds(LightningBolt entity) {
		return true;
	}
}
