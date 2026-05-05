package dev.latvian.mods.vidlib.integration.iris;

import net.irisshaders.iris.api.v0.IrisApi;

public class IrisIntegrationImpl extends IrisIntegration {
	@Override
	public boolean isShaderPackInUse() {
		return IrisApi.getInstance().isShaderPackInUse();
	}
}
