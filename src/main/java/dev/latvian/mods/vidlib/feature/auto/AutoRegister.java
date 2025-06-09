package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @see net.neoforged.neoforge.registries.DeferredRegister
 * @see EntityRendererHolder
 * @see BlockEntityRendererHolder
 * @see ServerCommandHolder
 * @see ClientCommandHolder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoRegister {
	Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

	record ScanData(IModInfo mod, Object value) {
	}

	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<ScanData>();

		AutoHelper.load(AutoRegister.class, EnumSet.of(ElementType.FIELD), (mod, classLoader, ad) -> {
			if (!AutoHelper.getEnumValues(ad, Dist.class, "value", AutoHelper.BOTH_SIDES).contains(FMLLoader.getDist())) {
				VidLib.LOGGER.info("Skipped @AutoRegister field " + ad.clazz().getClassName() + "." + ad.memberName());
				return;
			}

			var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);
			var value = AutoHelper.getStaticFieldValue(clazz, ad);

			if (value != null) {
				VidLib.LOGGER.info("Found @AutoRegister field " + clazz.getName() + "." + ad.memberName());
				list.add(new ScanData(mod, value));
			}
		});

		return list;
	});
}
