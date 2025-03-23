package dev.beast.mods.shimmer.feature.prop;

import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class Prop {
	public final PropType<?> type;
	public Level level;
	public int id;
	boolean removed;
	public int tick;
	public final Vector3d pos;
	public final Vector3d prevPos;
	public final Vector3d velocity;
	public final Vector3d rotation;
	public final Vector3d prevRotation;
	public final Vector3d velocityMultiplier;
	public final double gravity;

	public Prop(PropType<?> type) {
		this.type = type;
		this.id = 0;
		this.removed = false;
		this.tick = 0;
		this.pos = new Vector3d(0D, 0D, 0D);
		this.prevPos = new Vector3d(0D, 0D, 0D);
		this.velocity = new Vector3d(0D, 0D, 0D);
		this.rotation = new Vector3d(0D, 0D, 0D);
		this.prevRotation = new Vector3d(0D, 0D, 0D);
		this.velocityMultiplier = new Vector3d(0.98D, 1D, 0.98D);
		this.gravity = 0.08D;
	}

	public void remove() {
		removed = true;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void updatePrevious() {
		prevPos.set(pos);
		prevRotation.set(rotation);
	}

	public void tick() {
		move();
	}

	public void move() {
		pos.add(velocity);
		velocity.mul(velocityMultiplier);
		velocity.y -= gravity;
	}

	public void onAdded() {
	}

	public void onRemoved() {
	}
}
