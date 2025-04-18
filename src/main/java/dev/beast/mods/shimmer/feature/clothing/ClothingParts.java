package dev.beast.mods.shimmer.feature.clothing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.Lazy;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Function;

@AutoInit
public record ClothingParts(boolean head, boolean body, boolean legs, boolean feet, boolean enchanted) {
	public static final ClothingParts ALL = new ClothingParts(true, true, true, true, false);
	public static final ClothingParts NONE = new ClothingParts(false, false, false, false, false);

	public static final Codec<ClothingParts> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("head", true).forGetter(ClothingParts::head),
		Codec.BOOL.optionalFieldOf("body", true).forGetter(ClothingParts::body),
		Codec.BOOL.optionalFieldOf("legs", true).forGetter(ClothingParts::legs),
		Codec.BOOL.optionalFieldOf("feet", true).forGetter(ClothingParts::feet),
		Codec.BOOL.optionalFieldOf("enchanted", false).forGetter(ClothingParts::enchanted)
	).apply(instance, ClothingParts::new));

	public static final Codec<ClothingParts> CODEC = Codec.either(Codec.BOOL, DIRECT_CODEC).xmap(either -> either.map(b -> b ? ALL : NONE, Function.identity()), p -> p.equals(ALL) ? Either.left(true) : p.equals(NONE) ? Either.left(false) : Either.right(p));

	public static final StreamCodec<ByteBuf, ClothingParts> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ClothingParts decode(ByteBuf buf) {
			return new ClothingParts(buf.readByte() & 0xFF);
		}

		@Override
		public void encode(ByteBuf buf, ClothingParts value) {
			buf.writeByte(value.hashCode());
		}
	};

	public static final KnownCodec<ClothingParts> KNOWN_CODEC = KnownCodec.register(Shimmer.id("clothing_parts"), CODEC, STREAM_CODEC, ClothingParts.class);

	public static final Lazy<ItemStack> ENCHANTED_ITEM = Lazy.of(() -> {
		var stack = new ItemStack(Items.APPLE);
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return stack;
	});

	public ClothingParts(int flags) {
		this(
			(flags & 1) != 0,
			(flags & 2) != 0,
			(flags & 4) != 0,
			(flags & 8) != 0,
			(flags & 16) != 0
		);
	}

	public boolean visible(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> head();
			case CHEST -> body();
			case LEGS -> legs();
			case FEET -> feet();
			default -> false;
		};
	}

	@Override
	public int hashCode() {
		return 0
			| (head ? 1 : 0)
			| (body ? 2 : 0)
			| (legs ? 4 : 0)
			| (feet ? 8 : 0)
			| (enchanted ? 16 : 0);
	}

	public ItemStack getItem() {
		return enchanted ? ENCHANTED_ITEM.get() : ItemStack.EMPTY;
	}

	@Override
	public String toString() {
		if (head && body && legs && feet && !enchanted) {
			return "ClothingParts[all]";
		} else if (!head && !body && !legs && !feet && !enchanted) {
			return "ClothingParts[none]";
		} else {
			return "ClothingParts[" +
				(head ? "head" : "") +
				(body ? ",body" : "") +
				(legs ? ",legs" : "") +
				(feet ? ",feet" : "") +
				(enchanted ? ",enchanted" : "") +
				"]";
		}
	}
}
