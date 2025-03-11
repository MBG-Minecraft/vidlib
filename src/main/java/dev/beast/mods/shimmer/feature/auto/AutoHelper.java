package dev.beast.mods.shimmer.feature.auto;

import dev.beast.mods.shimmer.Shimmer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AutoHelper {
	public interface Callback {
		void accept(IModInfo mod, ClassLoader classLoader, ModFileScanData.AnnotationData ad) throws Exception;
	}

	public static void load(Class<? extends Annotation> annotation, Set<ElementType> elementTypes, Callback callback) {
		var annotationType = Type.getType(annotation);

		for (var mod : ModList.get().getMods()) {
			var owningFile = mod.getOwningFile();

			if (owningFile != null) {
				var file = owningFile.getFile();

				if (file != null) {
					ClassLoader classLoader = null;

					for (var ad : file.getScanResult().getAnnotations()) {
						if (elementTypes.contains(ad.targetType()) && ad.annotationType().equals(annotationType)) {
							try {
								if (classLoader == null) {
									classLoader = FMLLoader.getGameLayer().findLoader(owningFile.moduleName());
								}

								callback.accept(mod, classLoader, ad);
							} catch (Exception ex) {
								throw new RuntimeException("Failed to process @" + annotation.getSimpleName() + " for '" + mod.getDisplayName() + "' mod", ex);
							}
						}
					}
				}
			}
		}
	}

	public static final EnumSet<Dist> BOTH_SIDES = EnumSet.of(Dist.CLIENT, Dist.DEDICATED_SERVER);

	public static <E extends Enum<E>> E getEnumValue(ModFileScanData.AnnotationData ad, Class<E> enumClass, String name, E defaultValue) {
		var typeData = ad.annotationData().get(name);

		if (typeData == null) {
			return defaultValue;
		}

		var value = ((ModAnnotation.EnumHolder) typeData).value();

		for (var e : enumClass.getEnumConstants()) {
			if (e.name().equals(value)) {
				return e;
			}
		}

		return defaultValue;
	}

	public static <E extends Enum<E>> EnumSet<E> getEnumValues(ModFileScanData.AnnotationData ad, Class<E> enumClass, String name, EnumSet<E> defaultValues) {
		var typeData = ad.annotationData().get(name);

		if (typeData == null) {
			return defaultValues;
		}

		var values = new ArrayList<E>();

		@SuppressWarnings("unchecked")
		var list = ((List<ModAnnotation.EnumHolder>) typeData);
		var constants = enumClass.getEnumConstants();

		for (var holder : list) {
			for (var e : constants) {
				if (e.name().equals(holder.value())) {
					values.add(e);
					break;
				}
			}
		}

		return EnumSet.copyOf(values);
	}

	public static Method getMethod(Class<?> clazz, ModFileScanData.AnnotationData ad, ClassLoader classLoader) throws Exception {
		var argData = org.objectweb.asm.Type.getArgumentTypes(ad.memberName().substring(ad.memberName().indexOf('(')));
		var argTypes = new Class[argData.length];

		for (var i = 0; i < argData.length; i++) {
			argTypes[i] = Class.forName(argData[i].getClassName(), true, classLoader);
		}

		return clazz.getDeclaredMethod(ad.memberName().substring(0, ad.memberName().indexOf('(')), argTypes);
	}

	@Nullable
	public static Object getStaticFieldValue(Class<?> clazz, ModFileScanData.AnnotationData ad) throws Exception {
		var name = ad.memberName();

		try {
			var field = clazz.getDeclaredField(name);

			if (!Modifier.isPublic(field.getModifiers())) {
				try {
					field.trySetAccessible();
				} catch (Exception ex) {
				}
			}

			try {
				return field.get(null);
			} catch (Exception ex) {
				var method = clazz.getDeclaredMethod("get" + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1));

				try {
					return method.invoke(null);
				} catch (Exception ex2) {
					return method.invoke(clazz.getDeclaredField("INSTANCE").get(null));
				}
			}
		} catch (Exception ex) {
			Shimmer.LOGGER.error("Unable to access field " + clazz.getName() + "." + ad.memberName(), ex.getCause());
			return null;
		}
	}
}
