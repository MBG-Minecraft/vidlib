package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EmptyZone implements Zone {
	private static final AABB BOX = new AABB(0D, 0D, 0D, 0D, 0D, 0D);
	public static final EmptyZone INSTANCE = new EmptyZone();
	public static final ZoneType<EmptyZone> TYPE = new ZoneType<>("empty", MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	private EmptyZone() {
	}

	@Override
	public ZoneType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return BOX;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return false;
	}

	@Override
	public boolean contains(AABB box) {
		return false;
	}
}
