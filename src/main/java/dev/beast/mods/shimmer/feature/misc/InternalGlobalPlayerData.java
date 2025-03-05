package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.session.PlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class InternalGlobalPlayerData extends PlayerData {
	public static final Codec<InternalGlobalPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ComponentSerialization.CODEC.optionalFieldOf("nickname").forGetter(d -> d.nickname),
		ItemStack.OPTIONAL_CODEC.optionalFieldOf("plumbob", ItemStack.EMPTY).forGetter(d -> d.plumbob)
	).apply(instance, InternalGlobalPlayerData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, InternalGlobalPlayerData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
		d -> d.nickname,
		ItemStack.OPTIONAL_STREAM_CODEC,
		d -> d.plumbob,
		InternalGlobalPlayerData::new
	);

	public Optional<Component> nickname;
	public ItemStack plumbob;

	InternalGlobalPlayerData() {
		super(InternalPlayerData.GLOBAL);
		this.nickname = Optional.empty();
		this.plumbob = ItemStack.EMPTY;
	}

	private InternalGlobalPlayerData(
		Optional<Component> nickname,
		ItemStack plumbob
	) {
		super(InternalPlayerData.GLOBAL);
		this.nickname = nickname;
		this.plumbob = plumbob;
	}
}
