package dev.beast.mods.shimmer.feature.auto;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Lazy;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AutoInit {
	enum Type {
		REGISTRY(false, false),
		CLIENT_SETUP(true, false),
		AFTER_SETUP(false, false),

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

	Type value() default Type.AFTER_SETUP;

	record AutoMethod(Type type, Method method) {
	}

	@ApiStatus.Internal
	Lazy<List<AutoMethod>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<AutoMethod>();
		var classLoader = AutoInit.class.getModule().getClassLoader();

		for (var scan : ModList.get().getAllScanData()) {
			scan.getAnnotatedBy(AutoInit.class, ElementType.TYPE).forEach(ad -> {
				try {
					var typeData = ad.annotationData().get("value");
					var type = typeData == null ? Type.REGISTRY : Type.valueOf(((ModAnnotation.EnumHolder) typeData).value());

					if (type.clientOnly && !FMLLoader.getDist().isClient() || type.methodOnly) {
						Shimmer.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
						return;
					}

					var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);
					Shimmer.LOGGER.info("Found @AutoInit class " + clazz.getName());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			scan.getAnnotatedBy(AutoInit.class, ElementType.METHOD).forEach(ad -> {
				try {
					var typeData = ad.annotationData().get("value");
					var type = typeData == null ? Type.REGISTRY : Type.valueOf(((ModAnnotation.EnumHolder) typeData).value());

					if (type.clientOnly && !FMLLoader.getDist().isClient()) {
						Shimmer.LOGGER.info("Skipped @AutoInit method " + ad.clazz().getClassName() + "#" + ad.memberName());
						return;
					}

					var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);
					var argData = org.objectweb.asm.Type.getArgumentTypes(ad.memberName().substring(ad.memberName().indexOf('(')));
					var argTypes = new Class[argData.length];

					for (var i = 0; i < argData.length; i++) {
						argTypes[i] = Class.forName(argData[i].getClassName(), true, classLoader);
					}

					var method = clazz.getDeclaredMethod(ad.memberName().substring(0, ad.memberName().indexOf('(')), argTypes);
					Shimmer.LOGGER.info("Found @AutoInit method " + clazz.getName() + "#" + method.getName());
					list.add(new AutoMethod(type, method));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}

		return list;
	});
}
