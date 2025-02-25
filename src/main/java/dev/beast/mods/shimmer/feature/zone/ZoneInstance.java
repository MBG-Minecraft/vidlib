package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.util.IdentityKey;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class ZoneInstance {
	public static final Codec<ZoneInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		IdentityKey.CODEC.fieldOf("id").forGetter(ZoneInstance::id),
		Zone.CODEC.fieldOf("zone").forGetter(ZoneInstance::zone)
	).apply(instance, ZoneInstance::new));

	public static final StreamCodec<ByteBuf, ZoneInstance> STREAM_CODEC = StreamCodec.composite(
		IdentityKey.STREAM_CODEC,
		ZoneInstance::id,
		Zone.STREAM_CODEC,
		ZoneInstance::zone,
		ZoneInstance::new
	);

	public final IdentityKey id;
	public Zone zone;
	public final Set<Player> playersInside;
	public Object renderer;

	public ZoneInstance(IdentityKey id, Zone zone) {
		this.id = id;
		this.zone = zone;
		this.playersInside = new ReferenceLinkedOpenHashSet<>();
	}

	public IdentityKey id() {
		return id;
	}

	public Zone zone() {
		return zone;
	}
}
