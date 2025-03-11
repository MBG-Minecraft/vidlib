package dev.beast.mods.shimmer.feature.auto;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.Lazy;
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

	record ScanData(ShimmerPacketType<?> type, EnumSet<To> to) {
	}

	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<ScanData>();

		AutoHelper.load(AutoPacket.class, EnumSet.of(ElementType.FIELD), (mod, classLoader, ad) -> {
			var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);
			var type = (ShimmerPacketType<?>) AutoHelper.getStaticFieldValue(clazz, ad);
			var toList = AutoHelper.getEnumValues(ad, To.class, "value", To.DEFAULT);
			Shimmer.LOGGER.info("Found @AutoPacket " + clazz.getName() + "." + ad.memberName() + " to " + String.join(", ", toList.stream().map(p -> p.name().toLowerCase()).toList()));
			list.add(new ScanData(type, toList));
		});

		return list;
	});
}
