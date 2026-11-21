package dev.latvian.mods.vidlib.core;

import com.mojang.datafixers.util.Function4;
import dev.latvian.mods.klib.core.KLibExtensions;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.api.VLObject;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.function.Predicate;

public class VLExtensions {
	public static MethodHandle getMethodHandle;
	public static MethodHandle setMethodHandle;

	private static Class<?> findClass(String name) throws ClassNotFoundException {
		var classLoader = KLibExtensions.class.getClassLoader();
		ClassLoader previousClassLoader = null;

		while (classLoader != null && classLoader != previousClassLoader) {
			try {
				return Class.forName(name, true, classLoader);
			} catch (Throwable ignored) {
				previousClassLoader = classLoader;
				classLoader = classLoader.getParent();
			}
		}

		throw new ClassNotFoundException(name);
	}

	public static void init() {
		try {
			var lookup = MethodHandles.lookup();

			var c = findClass("dev.latvian.mods.vidlib.core.api.VLObject");
			getMethodHandle = lookup.findVirtual(c, "vl$get", MethodType.methodType(Object.class, int.class));
			setMethodHandle = lookup.findVirtual(c, "vl$set", MethodType.methodType(void.class, int.class, Object.class));

			var b = findClass("dev.latvian.mods.vidlib.core.api.VLBridge");
			lookup.findStaticVarHandle(b, "disableSleepStatusAnnouncement", Predicate.class).set((Predicate<ServerLevel>) level -> CommonGameEngine.INSTANCE.disableSleepStatusAnnouncement(level));
			lookup.findStaticVarHandle(b, "tickPrecipitation", Function4.class).set((Function4<ServerLevel, BlockPos, BlockPos, BlockPos, Void>) (level, blockPos, motionBlockedPos, belowMotionBlockedPos) -> {
				CommonGameEngine.INSTANCE.tickPrecipitation(level, blockPos, motionBlockedPos, belowMotionBlockedPos);
				return null;
			});
			VidLib.LOGGER.info("KLibExtensions initialized");
		} catch (Throwable ex) {
			throw new RuntimeException("Couldn't load KLibExtensions", ex);
		}
	}

	public static Object getRaw(Object object, int id) {
		if (object instanceof VLObject ext) {
			return ext.vl$get(id);
		}

		try {
			return getMethodHandle.invoke(object, id);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke VLObject#klib$get", ex);
		}
	}

	public static <T> T get(Object object, int id) {
		return (T) getRaw(object, id);
	}

	public static void set(Object object, int id, Object value) {
		if (object instanceof VLObject ext) {
			ext.vl$set(id, value);
			return;
		}

		try {
			setMethodHandle.invoke(object, id, value);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke VLObject#klib$set", ex);
		}
	}

	public static <K, V> V compute(K object, int id, Function<K, V> factory) {
		var value = getRaw(object, id);

		if (value == null) {
			value = factory.apply(object);
			set(object, id, value);
		}

		return (V) value;
	}
}
