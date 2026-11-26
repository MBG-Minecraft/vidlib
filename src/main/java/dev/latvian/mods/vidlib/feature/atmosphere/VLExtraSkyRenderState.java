package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.Range;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Quaternionfc;
import org.joml.Vector4f;

public class VLExtraSkyRenderState {
	public boolean sun = true;
	public boolean moon = true;
	public Quaternionfc celestialTilt = Identity.QUATERNION;
	public AbstractTexture skyboxTexture = null;
	public boolean skyboxEquirectangular = false;
	public Quaternionfc skyboxRotation = Identity.QUATERNION;
	public final Vector4f skyboxTint = new Vector4f(1F, 1F, 1F, 1F);
	public final Vector4f skyboxChromaKey = new Vector4f(0F, 0F, 0F, 0F);
	public Range skyboxChromaKeyRange = Range.FULL;
}
