package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.LevelReader;

public interface VLLevelReader extends VLOpsHolder {
	default LevelReader vl$levelReader() {
		return (LevelReader) this;
	}

	@Override
	default RegistryOps<Tag> nbtOps() {
		return vl$levelReader().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	@Override
	default RegistryOps<JsonElement> jsonOps() {
		return vl$levelReader().registryAccess().createSerializationContext(JsonOps.INSTANCE);
	}
}
