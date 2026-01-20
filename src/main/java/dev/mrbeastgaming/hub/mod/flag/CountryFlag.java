package dev.mrbeastgaming.hub.mod.flag;

import dev.latvian.mods.klib.util.Lazy;
import dev.mrbeastgaming.hub.api.Country;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.List;

public record CountryFlag(String code, Lazy<Country> country, Holder<BannerPattern> bannerPattern) {
	public BannerPatternLayers createBannerPatternLayers() {
		return new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(bannerPattern, DyeColor.WHITE)));
	}
}
