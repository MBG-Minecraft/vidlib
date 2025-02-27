package dev.beast.mods.shimmer.math;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class Split {
	public static final Vector3f[][] FACE_POS = new Vector3f[6][];
	public static final Vector3f[] NORMALS = new Vector3f[6];

	static {
		float mns = -0.5F;
		float mxs = 0.5F;

		var vWDN = new Vector3f(mns, mns, mns);
		var vEDN = new Vector3f(mxs, mns, mns);
		var vEDS = new Vector3f(mxs, mns, mxs);
		var vWDS = new Vector3f(mns, mns, mxs);
		var vWUN = new Vector3f(mns, mxs, mns);
		var vEUN = new Vector3f(mxs, mxs, mns);
		var vEUS = new Vector3f(mxs, mxs, mxs);
		var vWUS = new Vector3f(mns, mxs, mxs);

		FACE_POS[0] = new Vector3f[]{vWDS, vWDN, vEDN, vEDS}; // down
		FACE_POS[1] = new Vector3f[]{vWUN, vWUS, vEUS, vEUN}; // up
		FACE_POS[2] = new Vector3f[]{vEUN, vEDN, vWDN, vWUN}; // north
		FACE_POS[3] = new Vector3f[]{vWUS, vWDS, vEDS, vEUS}; // south
		FACE_POS[4] = new Vector3f[]{vWUN, vWDN, vWDS, vWUS}; // west
		FACE_POS[5] = new Vector3f[]{vEUS, vEDS, vEDN, vEUN}; // east

		for (int i = 0; i < 6; i++) {
			NORMALS[i] = Direction.VALUES[i].step();
		}
	}

	public final int id;
	public final int split;
	public final int count;
	public final SplitBox[] boxes;
	public final float scale;
	public final Vector3f min;
	public final Vector3f max;
	public final AABB box;

	public Split(int id) {
		this.id = id;
		this.split = 1 << id;
		this.count = split * split * split;
		this.boxes = new SplitBox[count];
		this.scale = 1F / (float) split;
		float mns = -scale / 2F;
		float mxs = scale / 2F;
		this.min = new Vector3f(mns, mns, mns);
		this.max = new Vector3f(mxs, mxs, mxs);
		this.box = new AABB(mns, mns, mns, mxs, mxs, mxs);
	}

	public void calculateUV(long seed, float weirdness) {
		var r = new XoroshiroRandomSource(seed);

		for (int x = 0; x < split; x++) {
			for (int y = 0; y < split; y++) {
				for (int z = 0; z < split; z++) {
					int i = x + z * split + y * split * split;

					float x0 = x * scale;
					float y0 = y * scale;
					float z0 = z * scale;
					float x1 = (x + 1F) * scale;
					float y1 = (y + 1F) * scale;
					float z1 = (z + 1F) * scale;

					boxes[i] = new SplitBox(this, i, (x + 0.5F) * scale, (y + 0.5F) * scale, (z + 0.5F) * scale, new UV[6], new Vector3f[6][]);

					boxes[i].uvs()[0] = new UV(x0, 1F - z1, x1, 1F - z0); // down
					boxes[i].uvs()[1] = new UV(x0, z0, x1, z1); // up
					boxes[i].uvs()[2] = new UV(x1, 1F - y1, x0, 1F - y0); // north
					boxes[i].uvs()[3] = new UV(x0, 1F - y1, x1, 1F - y0); // south
					boxes[i].uvs()[4] = new UV(z0, 1F - y1, z1, 1F - y0); // west
					boxes[i].uvs()[5] = new UV(1F - z1, 1F - y1, 1F - z0, 1F - y0); // east

					float wa = 1F - weirdness;
					float wm = weirdness * 2F;

					for (int f = 0; f < 6; f++) {
						float mns = -0.5F;
						float mxs = 0.5F;

						var vWDN = new Vector3f(mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEDN = new Vector3f(mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEDS = new Vector3f(mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWDS = new Vector3f(mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWUN = new Vector3f(mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEUN = new Vector3f(mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEUS = new Vector3f(mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWUS = new Vector3f(mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));

						boxes[i].facePos()[0] = new Vector3f[]{vWDS, vWDN, vEDN, vEDS}; // down
						boxes[i].facePos()[1] = new Vector3f[]{vWUN, vWUS, vEUS, vEUN}; // up
						boxes[i].facePos()[2] = new Vector3f[]{vEUN, vEDN, vWDN, vWUN}; // north
						boxes[i].facePos()[3] = new Vector3f[]{vWUS, vWDS, vEDS, vEUS}; // south
						boxes[i].facePos()[4] = new Vector3f[]{vWUN, vWDN, vWDS, vWUS}; // west
						boxes[i].facePos()[5] = new Vector3f[]{vEUS, vEDS, vEDN, vEUN}; // east
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Split_1/" + split;
	}
}
