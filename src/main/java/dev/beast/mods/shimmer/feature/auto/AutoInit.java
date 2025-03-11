package dev.beast.mods.shimmer.feature.auto;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Lazy;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AutoInit {
	enum Type {
		REGISTRY(false, false), // (IEventBus)
		CLIENT_SETUP(true, false), // ()
		LOAD_COMPLETE(false, false), // ()

		;

		public final boolean clientOnly;
		public final boolean methodOnly;

		Type(boolean clientOnly, boolean methodOnly) {
			this.clientOnly = clientOnly;
			this.methodOnly = methodOnly;
		}

		public void invoke(Object... args) {
			for (var s : SCANNED.get()) {
				if (s.type == this) {
					try {
						if (s.method.getParameterCount() == 0) {
							s.method.invoke(null);
						} else {
							s.method.invoke(null, args);
						}
					} catch (Exception ex) {
						Shimmer.LOGGER.error("Failed to invoke @AutoInit method " + s.method().getDeclaringClass().getName() + "#" + s.method().getName(), ex);
					}
				}
			}
		}
	}

	Type value() default Type.LOAD_COMPLETE;

	record AutoMethod(Type type, Method method) {
	}

	@ApiStatus.Internal
	Lazy<List<AutoMethod>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<AutoMethod>();

		AutoHelper.load(AutoInit.class, EnumSet.of(ElementType.TYPE, ElementType.METHOD), (mod, classLoader, ad) -> {
			var type = AutoHelper.getEnumValue(ad, Type.class, "value", Type.REGISTRY);

			if (type.clientOnly && !FMLLoader.getDist().isClient()) {
				Shimmer.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
				return;
			}

			if (type.methodOnly && ad.targetType() != ElementType.METHOD) {
				Shimmer.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
				return;
			}

			var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);

			if (ad.targetType() == ElementType.METHOD) {
				var method = AutoHelper.getMethod(clazz, ad, classLoader);
				Shimmer.LOGGER.info("Found @AutoInit method " + clazz.getName() + "#" + method.getName());
				list.add(new AutoMethod(type, method));
			} else {
				Shimmer.LOGGER.info("Found @AutoInit class " + clazz.getName());
			}
		});

		return list;
	});
}
