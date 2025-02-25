package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class UniverseZone implements Zone {
	public static final UniverseZone INSTANCE = new UniverseZone();
	public static final ZoneType<UniverseZone> TYPE = new ZoneType<>("universe", MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	private UniverseZone() {
	}

	@Override
	public ZoneType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return true;
	}

	@Override
	public boolean contains(AABB box) {
		return true;
	}
}
