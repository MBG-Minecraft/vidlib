package dev.beast.mods.shimmer.math;

import org.joml.Vector3f;

public class SpherePoints {
	public static final SpherePoints L = new SpherePoints(9, 7);
	public static final SpherePoints M = new SpherePoints(24, 9);
	public static final SpherePoints H = new SpherePoints(36, 16);
	public static final SpherePoints X = new SpherePoints(108, 27);

	public static SpherePoints get(int hd, int vd) {
		if (hd == 9 && vd == 7) {
			return L;
		} else if (hd == 24 && vd == 9) {
			return M;
		} else if (hd == 36 && vd == 16) {
			return H;
		} else if (hd == 108 && vd == 27) {
			return X;
		} else {
			return new SpherePoints(hd, vd);
		}
	}

	public record Col(int index, float u, float x, float z) {
	}

	public record Row(int index, float v, float y, float m) {
	}

	public final int hdetail;
	public final int vdetail;
	public final Col[] cols;
	public final Row[] rows;
	public final Vector3f[][] normals;

	private SpherePoints(int hd, int vd) {
		this.hdetail = hd;
		this.vdetail = vd;
		this.cols = new Col[hd + 1];
		this.rows = new Row[vd + 1];
		this.normals = new Vector3f[hd][vd];
		calculate();
	}

	public void calculate() {
		for (int i = 0; i < cols.length; i++) {
			double d = i / (cols.length - 1D);
			this.cols[i] = new Col(i, (float) (1D - d), (float) (Math.cos(d * Math.PI * 2D) * 0.5D), (float) (Math.sin(d * Math.PI * 2D) * 0.5D));
		}

		for (int i = 1; i < rows.length - 1; i++) {
			double d = i / (rows.length - 1D);
			this.rows[i] = new Row(i, (float) d, (float) (Math.cos(d * Math.PI) * 0.5D), (float) (((Math.sin(d * Math.PI) + 1D) / 2D - 0.5D) * 2D));
		}

		rows[0] = new Row(0, 0F, 0.5F, 0F);
		rows[rows.length - 1] = new Row(rows.length - 1, 1F, -0.5F, 0F);

		for (int r = 0; r < rows.length - 1; r++) {
			for (int c = 0; c < cols.length - 1; c++) {
				var cr = rows[r];
				var nr = rows[r + 1];
				var cc = cols[c];
				var nc = cols[c + 1];

				// var n = new Vector3f(cr.x * nc.m, cr.y, cr.z * nc.m).cross(new Vector3f(nr.x * cc.m, nr.y, nr.z * cc.m)).normalize();
				normals[c][r] = new Vector3f(0F, 1F, 0F);
			}
		}

		/* FIXME
		for (int r = 0; r < rows.length - 1; r++) {
			for (int c = 0; c < cols.length - 1; c++) {
				var cr = rows[r];
				var nr = rows[r + 1];
				var cc = cols[c];
				var nc = cols[c + 1];

				var va = new Vector3f(cc.x * nr.m, nr.y, cc.z * nr.m);
				var vb = new Vector3f(cc.x * cr.m, cr.y, cc.z * cr.m);
				var vc = new Vector3f(nc.x * cr.m, cr.y, nc.z * cr.m);
				var vd = new Vector3f(nc.x * nr.m, nr.y, nc.z * nr.m);

				// normals[c][r] = vb.sub(va).mul(vc.sub(vb)).normalize();
			}
		}
		 */
	}
}