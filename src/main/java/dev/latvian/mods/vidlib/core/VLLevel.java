package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.bulk.BlockModificationConsumer;
import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.function.Consumer;

public interface VLLevel extends VLMinecraftEnvironmentDataHolder {
	@Override
	default Level vl$level() {
		return (Level) this;
	}

	default LevelSessionData<?> vl$getLevelData() {
		throw new NoMixinException(this);
	}

	default int bulkModify(boolean undoable, Ref<BulkLevelModification> modification) {
		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply((Level) this, modification.value(), undoable, false);
	}

	default int bulkModify(boolean undoable, Consumer<BlockModificationConsumer> modifications) {
		var m = new BulkLevelModificationBundle(new ArrayList<>());
		modifications.accept(m);
		return bulkModify(undoable, m.ref());
	}
}
