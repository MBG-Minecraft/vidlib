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
 * @see EntityRendererHolder
 * @see BlockEntityRendererHolder
 * @see ClientCommandHolder
 * @see dev.latvian.mods.vidlib.feature.canvas.Canvas
 * @see dev.latvian.mods.vidlib.feature.prop.PropRenderer.Holder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ClientAutoRegister {
	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		VidLib.LOGGER.info("Scanning @ClientAutoRegister...");
		var list = new ArrayList<ScanData>();

		AutoHelper.load(ClientAutoRegister.class, EnumSet.of(ElementType.FIELD), (source, classLoader, ad) -> {
			var clazz = AutoHelper.initClass(ad, classLoader);
			var value = AutoHelper.getStaticFieldValue(clazz, ad);

			if (value != null) {
				VidLib.LOGGER.info("Found @ClientAutoRegister field " + clazz.getName() + "." + ad.memberName());
				list.add(new ScanData(source, value));
			}
		});

		return list;
	});
}
