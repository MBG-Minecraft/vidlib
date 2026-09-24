package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.DynamicOps;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import net.minecraft.resources.DelegatingOps;

public class HubOps<O> extends DelegatingOps<O> {
	public HubResponseContext ctx;

	public HubOps(DynamicOps<O> ops) {
		super(ops);
		this.ctx = HubResponseContext.EMPTY;
	}
}
