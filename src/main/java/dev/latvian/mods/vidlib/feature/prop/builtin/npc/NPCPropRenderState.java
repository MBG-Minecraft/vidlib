package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class NPCPropRenderState extends PropRenderState<NPCProp> {
	public final AvatarRenderState avatarState = new AvatarRenderState();
}
