package dev.latvian.mods.vidlib.feature.prop.builtin.image;

import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class CachedImageData {
	public final Vector3f va, vb, vc, vd;
	public AABB box;

	public CachedImageData() {
		this.va = new Vector3f();
		this.vb = new Vector3f();
		this.vc = new Vector3f();
		this.vd = new Vector3f();
		this.box = AABB.INFINITE;
	}
}
