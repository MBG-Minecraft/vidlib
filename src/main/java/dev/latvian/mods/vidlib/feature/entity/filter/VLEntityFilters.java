package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public interface VLEntityFilters {
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF = EntityFilter.simple("staff", entity -> entity instanceof Player player && PlayerSessionData.of(player).isStaff());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> TALENT = EntityFilter.simple("talent", entity -> entity instanceof Player player && PlayerSessionData.of(player).isTalent());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF_OR_TALENT = EntityFilter.simple("staff_or_talent", entity -> entity instanceof Player player && PlayerSessionData.of(player).isStaffOrTalent());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SUSPENDED = EntityFilter.simple("suspended", entity -> CommonGameEngine.INSTANCE.isSuspended(entity));

	static void register(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, EntityFilter> registry) {
		registry.register(STAFF);
		registry.register(TALENT);
		registry.register(STAFF_OR_TALENT);
		registry.register(SUSPENDED);

		registry.register(ServerDataEntityFilter.TYPE);
		registry.register(LevelDataEntityFilter.TYPE);
		registry.register(PlayerDataEntityFilter.TYPE);

		EntityFilter.PREFIX_LIST.add("server_data:", DataKey.GLOBAL.dataKeyCodec, ServerDataEntityFilter::new);
		EntityFilter.PREFIX_LIST.add("level_data:", DataKey.LEVEL.dataKeyCodec, LevelDataEntityFilter::new);
		EntityFilter.PREFIX_LIST.add("player_data:", DataKey.PLAYER.dataKeyCodec, PlayerDataEntityFilter::new);
	}
}
