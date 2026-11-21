package dev.latvian.mods.vidlib.core.api;

import com.mojang.datafixers.util.Function4;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Predicate;

public class VLBridge {
	public static Predicate<ServerLevel> disableSleepStatusAnnouncement = _ -> false;
	public static Function4<ServerLevel, BlockPos, BlockPos, BlockPos, Void> tickPrecipitation = (_, _, _, _) -> null;
}
