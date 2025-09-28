package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
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
 * @see ServerCommandHolder
 * @see dev.latvian.mods.vidlib.feature.prop.PropType
 * @see dev.latvian.mods.vidlib.feature.item.VidLibTool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoRegister {
	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		VidLib.LOGGER.info("Scanning @AutoRegister...");
		var list = new ArrayList<ScanData>();

		AutoHelper.load(AutoRegister.class, EnumSet.of(ElementType.FIELD), (source, classLoader, ad) -> {
			var clazz = AutoHelper.initClass(ad, classLoader);
			var value = AutoHelper.getStaticFieldValue(clazz, ad);

			if (value != null) {
				VidLib.LOGGER.info("Found @AutoRegister field " + clazz.getName() + "." + ad.memberName());
				list.add(new ScanData(source, value));
			}
		});

		return list;
	});
}
