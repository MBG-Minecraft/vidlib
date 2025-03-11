package dev.beast.mods.shimmer.feature.auto;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.Lazy;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.modscan.ModAnnotation;
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
		SERVER_CONFIG
	}

	To[] value() default To.CLIENT;

	record ScanData(ShimmerPacketType<?> type, EnumSet<To> to) {
	}

	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<ScanData>();
		var classLoader = AutoPacket.class.getModule().getClassLoader();

		for (var scan : ModList.get().getAllScanData()) {
			scan.getAnnotatedBy(AutoPacket.class, ElementType.FIELD).forEach(ad -> {
				try {
					var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);
					var field = clazz.getDeclaredField(ad.memberName());
					var type = (ShimmerPacketType<?>) field.get(null);
					var toData = ad.annotationData().get("value");
					var toList = new ArrayList<To>(2);

					if (toData == null) {
						toList.add(To.CLIENT);
					} else {
						@SuppressWarnings("unchecked")
						var toListData = (List<ModAnnotation.EnumHolder>) toData;

						for (var holder : toListData) {
							toList.add(To.valueOf(holder.value()));
						}
					}

					Shimmer.LOGGER.info("Found @AutoPacket " + clazz.getName() + "." + ad.memberName() + " to " + String.join(", ", toList.stream().map(p -> p.name().toLowerCase()).toList()));
					list.add(new ScanData(type, EnumSet.copyOf(toList)));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}

		return list;
	});
}
