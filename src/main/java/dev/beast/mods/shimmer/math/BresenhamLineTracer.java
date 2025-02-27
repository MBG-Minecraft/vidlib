package dev.beast.mods.shimmer.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Iterator;

// https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
// https://www.youtube.com/watch?v=RGB-wlatStc
public class BresenhamLineTracer implements Iterable<BlockPos> {
	public static class BLTIterator implements Iterator<BlockPos> {
		private final BresenhamLineTracer tracer;
		private final BlockPos.MutableBlockPos voxel;
		private int iteration, error1, error2;

		public BLTIterator(BresenhamLineTracer tracer) {
			this.tracer = tracer;
			this.voxel = tracer.start.mutable();
			this.iteration = 0;
			this.error1 = tracer.startError1;
			this.error2 = tracer.startError2;
		}

		@Override
		public boolean hasNext() {
			return this.iteration < tracer.length;
		}

		@Override
		public BlockPos next() {
			final BlockPos ret = this.voxel.immutable();

			if (this.hasNext()) {
				this.traverse();
				this.iteration++;
			}

			return ret;
		}

		private void traverse() {
			switch (tracer.axis) {
				case X -> {
					if (this.error1 > 0) {
						this.bumpY();
						this.error1 -= tracer.doubleAbsDx;
					}
					if (this.error2 > 0) {
						this.bumpZ();
						this.error2 -= tracer.doubleAbsDx;
					}

					this.error1 += tracer.doubleAbsDy;
					this.error2 += tracer.doubleAbsDz;

					this.bumpX();
				}
				case Y -> {
					if (this.error1 > 0) {
						this.bumpX();
						this.error1 -= tracer.doubleAbsDy;
					}
					if (this.error2 > 0) {
						this.bumpZ();
						this.error2 -= tracer.doubleAbsDy;
					}

					this.error1 += tracer.doubleAbsDx;
					this.error2 += tracer.doubleAbsDz;

					this.bumpY();
				}
				case Z -> {
					if (this.error1 > 0) {
						this.bumpY();
						this.error1 -= tracer.doubleAbsDz;
					}
					if (this.error2 > 0) {
						this.bumpX();
						this.error2 -= tracer.doubleAbsDz;
					}

					this.error1 += tracer.doubleAbsDy;
					this.error2 += tracer.doubleAbsDx;

					this.bumpZ();
				}
			}
		}

		private void bumpX() {
			this.voxel.move(tracer.xD, 0, 0);
		}

		private void bumpY() {
			this.voxel.move(0, tracer.yD, 0);
		}

		private void bumpZ() {
			this.voxel.move(0, 0, tracer.zD);
		}
	}

	public final BlockPos start;
	public final int xD, yD, zD, doubleAbsDx, doubleAbsDy, doubleAbsDz, length;
	public final Direction.Axis axis;
	public final int startError1, startError2;

	public BresenhamLineTracer(BlockPos start, BlockPos dest) {
		this(start, dest.getX() - start.getX(), dest.getY() - start.getY(), dest.getZ() - start.getZ());
	}

	public BresenhamLineTracer(BlockPos start, int xDiff, int yDiff, int zDiff) {
		this.start = start;

		this.xD = (xDiff < 0) ? -1 : 1;
		this.yD = (yDiff < 0) ? -1 : 1;
		this.zD = (zDiff < 0) ? -1 : 1;

		int absDx = Math.abs(xDiff);
		int absDy = Math.abs(yDiff);
		int absDz = Math.abs(zDiff);

		this.doubleAbsDx = absDx << 1;
		this.doubleAbsDy = absDy << 1;
		this.doubleAbsDz = absDz << 1;

		if (absDx >= absDy && absDx >= absDz) {
			this.startError1 = this.doubleAbsDy - absDx;
			this.startError2 = this.doubleAbsDz - absDx;

			this.axis = Direction.Axis.X;
			this.length = absDx + 1;
		} else if (absDy >= absDx && absDy >= absDz) {
			this.startError1 = this.doubleAbsDy - absDy;
			this.startError2 = this.doubleAbsDz - absDy;

			this.axis = Direction.Axis.Y;
			this.length = absDy + 1;
		} else {
			this.startError1 = this.doubleAbsDy - absDz;
			this.startError2 = this.doubleAbsDz - absDz;

			this.axis = Direction.Axis.Z;
			this.length = absDz + 1;
		}
	}

	@Override
	public Iterator<BlockPos> iterator() {
		return new BLTIterator(this);
	}
}