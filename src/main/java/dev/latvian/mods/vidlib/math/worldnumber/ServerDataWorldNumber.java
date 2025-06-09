package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record ServerDataWorldNumber(String key, Supplier<DataKey<?>> dataKey) implements WorldNumber {
	public static final SimpleRegistryType<ServerDataWorldNumber> TYPE = SimpleRegistryType.dynamic("server_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ServerDataWorldNumber::key)
	).apply(instance, ServerDataWorldNumber::new)), ByteBufCodecs.STRING_UTF8.map(ServerDataWorldNumber::new, ServerDataWorldNumber::key));

	public ServerDataWorldNumber(String key) {
		this(key, Lazy.of(() -> DataKey.SERVER.all.get(key)));
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
		if (ctx.serverDataMap == null) {
			return null;
		}

		var dk = dataKey.get();
		var data = ctx.serverDataMap.get(dk);

		if (data == null) {
			return null;
		}

		var num = dk.type().toNumber(Cast.to(data));
		return num == null ? null : num instanceof Double d ? d : num.doubleValue();
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + key;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
