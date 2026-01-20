package dev.latvian.mods.vidlib.util;

import net.minecraft.core.Position;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class LevelOfDetailValue {
	public enum Type {
		NEVER,
		ALWAYS,
		WITHIN_DISTANCE
	}

	public static final LevelOfDetailValue PLAYER_ARMOR = new LevelOfDetailValue(false, 128D);
	public static final LevelOfDetailValue HELD_ITEM = new LevelOfDetailValue(false, 64D);
	public static final LevelOfDetailValue CLOTHING = new LevelOfDetailValue(false, 96D);
	public static final LevelOfDetailValue ENTITY_DETAILS = new LevelOfDetailValue(false, 96D);
	public static final LevelOfDetailValue ENTITY_ARMOR = new LevelOfDetailValue(false, 128D);
	public static final LevelOfDetailValue BLOCK_ENTITIES = new LevelOfDetailValue(false, 64, false);

	// public static final Codec<LevelOfDetailValue> CODEC = Codec.either(Codec.BOOL, Codec.DOUBLE);
	private static final double MAX_DISTANCE = 8192D;

	public final boolean is2D;
	public Type type;

	private boolean canBeAlways;
	private double distance;

	public LevelOfDetailValue(boolean is2D, double distance) {
		this(is2D, distance, true);
	}

	public LevelOfDetailValue(boolean is2D, double distance, boolean canBeAlways) {
		this.is2D = is2D;
		this.type = Type.WITHIN_DISTANCE;
		this.canBeAlways = canBeAlways;
		this.distance = distance;
	}

	public void setNeverVisible() {
		type = Type.NEVER;
	}

	public void setAlwaysVisible() {
		type = Type.ALWAYS;
	}

	public void setVisibleWithin() {
		type = Type.WITHIN_DISTANCE;
	}

	public Type getType() {
		return type;
	}

	public double getDistance() {
		return distance;
	}

	public boolean canBeAlways() {
		return canBeAlways;
	}

	public void setDistance(double d) {
		distance = Math.clamp(d, 0D, MAX_DISTANCE);
	}

	public boolean isVisible(Position camera, double x, double y, double z) {
		if (type == Type.NEVER) {
			return false;
		} else if (type == Type.ALWAYS) {
			return true;
		} else if (is2D) {
			double dx = camera.x() - x;
			double dz = camera.z() - z;
			return dx * dz + dz * dz <= distance * distance;
		} else {
			return Vector3d.distanceSquared(camera.x(), camera.y(), camera.z(), x, y, z) <= distance * distance;
		}
	}

	public boolean isVisible(Position camera, Position position) {
		return isVisible(camera, position.x(), position.y(), position.z());
	}

	public boolean isVisible(Position camera, Vector3dc position) {
		return isVisible(camera, position.x(), position.y(), position.z());
	}
}
