package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoPacket {
	enum To {
		CLIENT,
		SERVER,
		CLIENT_CONFIG,
		SERVER_CONFIG;

		public static final EnumSet<To> DEFAULT = EnumSet.of(CLIENT);
	}

	To[] value() default To.CLIENT;

	record ScanData(VidLibPacketType<?> type, EnumSet<To> to) {
	}

	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<ScanData>();
		VidLib.LOGGER.info("Scanning @AutoPacket...");

		AutoHelper.load(AutoPacket.class, EnumSet.of(ElementType.FIELD), (source, classLoader, ad) -> {
			var clazz = AutoHelper.initClass(ad, classLoader);
				var type = AutoHelper.getStaticFieldValue(clazz, ad);

			if (type instanceof VidLibPacketType<?> t) {
					var toList = AutoHelper.getEnumValues(ad, To.class, "value", To.DEFAULT);
				VidLib.LOGGER.info("Found @AutoPacket " + clazz.getName() + "." + ad.memberName() + " to " + String.join(", ", toList.stream().map(p -> p.name().toLowerCase()).toList()));
					list.add(new ScanData(t, toList));
				}
			}
		);

		return list;
	});
}
