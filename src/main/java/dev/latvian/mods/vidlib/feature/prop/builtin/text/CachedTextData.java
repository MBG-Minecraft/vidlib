package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class CachedTextData {
	public final FormattedCharSequence[] lines;
	public final int[] width;
	public int totalWidth;
	public final Vector3f va, vb, vc, vd;
	public AABB box;

	public CachedTextData(FormattedCharSequence[] lines) {
		this.lines = lines;
		this.width = new int[lines.length];
		this.totalWidth = 0;
		this.va = new Vector3f();
		this.vb = new Vector3f();
		this.vc = new Vector3f();
		this.vd = new Vector3f();
		this.box = AABB.INFINITE;
	}
}
