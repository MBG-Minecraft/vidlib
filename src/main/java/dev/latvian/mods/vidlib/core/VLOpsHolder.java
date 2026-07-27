package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

public interface VLOpsHolder {
	default DynamicOps<Tag> nbtOps() {
		throw new NoMixinException(this);
	}

	default DynamicOps<JsonElement> jsonOps() {
		throw new NoMixinException(this);
	}

	default TagParser<Tag> nbtParser() {
		return TagParser.create(nbtOps());
	}
}
