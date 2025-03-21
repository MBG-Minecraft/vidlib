package dev.beast.mods.shimmer.feature.clothing;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

@AutoInit
public class Clothing {
	public static final SimpleRegistry<Clothing> REGISTRY = SimpleRegistry.create(Clothing::type);
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = REGISTRY.registerUnitSuggestionProvider(Shimmer.id("clothing"));
	public static final Codec<Clothing> CODEC = REGISTRY.valueCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Clothing> STREAM_CODEC = REGISTRY.valueStreamCodec();
	public static final KnownCodec<Clothing> KNOWN_CODEC = KnownCodec.register(Shimmer.id("clothing"), CODEC, STREAM_CODEC, Clothing.class);

	public static Clothing register(ResourceLocation id) {
		var clothing = new Clothing(id);
		clothing.type = SimpleRegistryType.unit(id, clothing);
		REGISTRY.register(clothing.type);
		return clothing;
	}

	public static final Clothing NONE = register(Shimmer.id("none"));
	public static final Clothing HOST = register(Shimmer.id("host"));
	public static final Clothing HOST_WITH_MASK = register(Shimmer.id("host_with_mask"));

	public static final Clothing BLACK_TRACKSUIT = register(Shimmer.id("tracksuit/black"));
	public static final Clothing WHITE_TRACKSUIT = register(Shimmer.id("tracksuit/white"));
	public static final Clothing RED_TRACKSUIT = register(Shimmer.id("tracksuit/red"));
	public static final Clothing PINK_TRACKSUIT = register(Shimmer.id("tracksuit/pink"));
	public static final Clothing MAGENTA_TRACKSUIT = register(Shimmer.id("tracksuit/magenta"));
	public static final Clothing PURPLE_TRACKSUIT = register(Shimmer.id("tracksuit/purple"));
	public static final Clothing BLUE_TRACKSUIT = register(Shimmer.id("tracksuit/blue"));
	public static final Clothing CYAN_TRACKSUIT = register(Shimmer.id("tracksuit/cyan"));
	public static final Clothing GREEN_TRACKSUIT = register(Shimmer.id("tracksuit/green"));
	public static final Clothing LIME_TRACKSUIT = register(Shimmer.id("tracksuit/lime"));
	public static final Clothing YELLOW_TRACKSUIT = register(Shimmer.id("tracksuit/yellow"));
	public static final Clothing ORANGE_TRACKSUIT = register(Shimmer.id("tracksuit/orange"));

	public static final Clothing[] COLORED_TRACKSUITS = {
		RED_TRACKSUIT,
		PINK_TRACKSUIT,
		MAGENTA_TRACKSUIT,
		PURPLE_TRACKSUIT,
		BLUE_TRACKSUIT,
		CYAN_TRACKSUIT,
		GREEN_TRACKSUIT,
		LIME_TRACKSUIT,
		YELLOW_TRACKSUIT,
		ORANGE_TRACKSUIT
	};

	public static final Clothing SQUID_TRACKSUIT = register(Shimmer.id("tracksuit/squid"));

	public final ResourceLocation id;
	public final ResourceKey<EquipmentAsset> equipmentAsset;
	private SimpleRegistryType<Clothing> type;
	public ItemStack item;

	private Clothing(ResourceLocation id) {
		this.id = id;
		this.equipmentAsset = ResourceKey.create(EquipmentAssets.ROOT_ID, id);
		this.item = ItemStack.EMPTY;
	}

	public SimpleRegistryType<Clothing> type() {
		return type;
	}

	public Clothing item(ItemStack item) {
		this.item = item;
		return this;
	}
}
