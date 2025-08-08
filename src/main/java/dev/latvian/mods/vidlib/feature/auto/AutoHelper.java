package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import org.jetbrains.annotations.Nullable;

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
	public static void load(Class<? extends Annotation> annotation, Set<ElementType> elementTypes, AutoCallback callback) {
		PlatformHelper.CURRENT.load(annotation, elementTypes, callback);
	}

	public static final EnumSet<Side> BOTH_SIDES = EnumSet.of(Side.CLIENT, Side.SERVER);

	public static <E extends Enum<E>> E getEnumValue(ScannedAnnotation ad, Class<E> enumClass, String name, E defaultValue) {
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

	public static <E extends Enum<E>> EnumSet<E> getEnumValues(ScannedAnnotation ad, Class<E> enumClass, String name, EnumSet<E> defaultValues) {
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

	public static Method getMethod(Class<?> clazz, ScannedAnnotation ad, ClassLoader classLoader) throws Exception {
		var argData = org.objectweb.asm.Type.getArgumentTypes(ad.memberName().substring(ad.memberName().indexOf('(')));
		var argTypes = new Class[argData.length];

		for (var i = 0; i < argData.length; i++) {
			argTypes[i] = Class.forName(argData[i].getClassName(), true, classLoader);
		}

		return clazz.getDeclaredMethod(ad.memberName().substring(0, ad.memberName().indexOf('(')), argTypes);
	}

	@Nullable
	public static Object getStaticFieldValue(Class<?> clazz, ScannedAnnotation ad) throws Exception {
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
			VidLib.LOGGER.error("Unable to access field " + clazz.getName() + "." + ad.memberName(), ex.getCause());
			return null;
		}
	}
}
