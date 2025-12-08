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
import java.util.Locale;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoPacket {
	enum To {
		CLIENT,
		SERVER;

		public static final EnumSet<To> DEFAULT = EnumSet.of(CLIENT);

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	enum Stage {
		CONFIG,
		GAME;

		public static final EnumSet<Stage> DEFAULT = EnumSet.of(GAME);

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	To[] value() default To.CLIENT;

	Stage[] stage() default Stage.GAME;

	record ScanData(String className, VidLibPacketType<?> type, EnumSet<To> to, EnumSet<Stage> stage) {
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
				var stageList = AutoHelper.getEnumValues(ad, Stage.class, "stage", Stage.DEFAULT);
				list.add(new ScanData(clazz.getName(), t, toList, stageList));
			}
		});

		return list;
	});
}
