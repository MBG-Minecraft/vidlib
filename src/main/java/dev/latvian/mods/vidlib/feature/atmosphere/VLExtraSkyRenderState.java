package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.math.Identity;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Quaternionfc;
import org.joml.Vector4f;

public class VLExtraSkyRenderState {
	public boolean sun = true;
	public boolean moon = true;
	public Quaternionfc celestialTilt = Identity.QUATERNION;
	public AbstractTexture skyboxTexture = null;
	public Quaternionfc skyboxRotation = Identity.QUATERNION;
	public final Vector4f skyboxTint = new Vector4f(1F, 1F, 1F, 1F);
}
