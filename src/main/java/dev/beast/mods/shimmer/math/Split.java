package dev.beast.mods.shimmer.math;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.AABB;

public class Split {
	public static final Vec3f[][] FACE_POS = new Vec3f[6][];
	public static final Vec3f[] NORMALS = new Vec3f[6];

	static {
		float mns = -0.5F;
		float mxs = 0.5F;

		var vWDN = Vec3f.of(mns, mns, mns);
		var vEDN = Vec3f.of(mxs, mns, mns);
		var vEDS = Vec3f.of(mxs, mns, mxs);
		var vWDS = Vec3f.of(mns, mns, mxs);
		var vWUN = Vec3f.of(mns, mxs, mns);
		var vEUN = Vec3f.of(mxs, mxs, mns);
		var vEUS = Vec3f.of(mxs, mxs, mxs);
		var vWUS = Vec3f.of(mns, mxs, mxs);

		FACE_POS[0] = new Vec3f[]{vWDS, vWDN, vEDN, vEDS}; // down
		FACE_POS[1] = new Vec3f[]{vWUN, vWUS, vEUS, vEUN}; // up
		FACE_POS[2] = new Vec3f[]{vEUN, vEDN, vWDN, vWUN}; // north
		FACE_POS[3] = new Vec3f[]{vWUS, vWDS, vEDS, vEUS}; // south
		FACE_POS[4] = new Vec3f[]{vWUN, vWDN, vWDS, vWUS}; // west
		FACE_POS[5] = new Vec3f[]{vEUS, vEDS, vEDN, vEUN}; // east

		for (int i = 0; i < 6; i++) {
			NORMALS[i] = Vec3f.of(Direction.VALUES[i].step());
		}
	}

	public final int id;
	public final int split;
	public final int count;
	public final SplitBox[] boxes;
	public final float scale;
	public final Vec3f min;
	public final Vec3f max;
	public final AABB box;

	public Split(int id) {
		this.id = id;
		this.split = 1 << id;
		this.count = split * split * split;
		this.boxes = new SplitBox[count];
		this.scale = 1F / (float) split;
		float mns = -scale / 2F;
		float mxs = scale / 2F;
		this.min = Vec3f.of(mns, mns, mns);
		this.max = Vec3f.of(mxs, mxs, mxs);
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

					boxes[i] = new SplitBox(this, i, (x + 0.5F) * scale, (y + 0.5F) * scale, (z + 0.5F) * scale, new UV[6], new Vec3f[6][]);

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

						var vWDN = Vec3f.of(mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEDN = Vec3f.of(mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEDS = Vec3f.of(mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWDS = Vec3f.of(mns * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWUN = Vec3f.of(mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEUN = Vec3f.of(mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mns * (wa + r.nextFloat() * wm));
						var vEUS = Vec3f.of(mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));
						var vWUS = Vec3f.of(mns * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm), mxs * (wa + r.nextFloat() * wm));

						boxes[i].facePos()[0] = new Vec3f[]{vWDS, vWDN, vEDN, vEDS}; // down
						boxes[i].facePos()[1] = new Vec3f[]{vWUN, vWUS, vEUS, vEUN}; // up
						boxes[i].facePos()[2] = new Vec3f[]{vEUN, vEDN, vWDN, vWUN}; // north
						boxes[i].facePos()[3] = new Vec3f[]{vWUS, vWDS, vEDS, vEUS}; // south
						boxes[i].facePos()[4] = new Vec3f[]{vWUN, vWDN, vWDS, vWUS}; // west
						boxes[i].facePos()[5] = new Vec3f[]{vEUS, vEDS, vEDN, vEUN}; // east
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
