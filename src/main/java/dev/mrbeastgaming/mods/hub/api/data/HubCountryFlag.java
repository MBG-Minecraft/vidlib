package dev.mrbeastgaming.mods.hub.api.data;

import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.List;

public record HubCountryFlag(String code, Lazy<HubCountry> country, Holder<BannerPattern> bannerPattern) {
	public BannerPatternLayers createBannerPatternLayers() {
		return new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(bannerPattern, DyeColor.WHITE)));
	}
}
