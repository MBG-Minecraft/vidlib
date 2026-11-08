package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.bulk.BlockModificationBuilder;
import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface VLServerLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this.vl$level().getServer();
	}

	@Override
	default ServerLevel vl$level() {
		return (ServerLevel) this;
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return this.vl$level().players();
	}

	@Override
	default int bulkModify(boolean undoable, Ref<BulkLevelModification> modification) {
		var optimized = modification.optimizedRef();

		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		if (optimized.value() instanceof BulkLevelModificationBundle b) {
			var builder = new BlockModificationBuilder();

			for (var m : b.list()) {
				m.value().apply(builder);
			}

			optimized = builder.build();
		}

		return VLLevel.super.bulkModify(undoable, optimized);
	}
}
