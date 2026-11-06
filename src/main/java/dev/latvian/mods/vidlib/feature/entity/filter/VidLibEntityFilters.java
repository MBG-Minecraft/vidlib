package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface VidLibEntityFilters {
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF = EntityFilter.simple("staff", entity -> VLPlatformHelper.INSTANCE.isStaff(entity));
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF_OR_TALENT = EntityFilter.simple("staff_or_talent", entity -> VLPlatformHelper.INSTANCE.isStaffOrTalent(entity));

	static void register(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, EntityFilter> registry) {
		registry.register(STAFF);
		registry.register(STAFF_OR_TALENT);

		registry.register(PlayerDataEntityFilter.TYPE);
		registry.register(ServerDataEntityFilter.TYPE);

		EntityFilter.PREFIX_LIST.add("$$", DataKey.PLAYER.dataKeyCodec, PlayerDataEntityFilter::new);
		EntityFilter.PREFIX_LIST.add("$", DataKey.SERVER.dataKeyCodec, ServerDataEntityFilter::new);
	}
}
