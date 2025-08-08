package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface AutoInit {
	enum Type {
		DEFAULT(false, false), // ()
		GAME_LOADED(false, false), // ()
		CLIENT_LOADED(true, false), // ()
		ASSETS_LOADED(true, false), // (ResourceManager)
		SERVER_STARTED(false, false), // (MinecraftServer)
		DATA_LOADED(false, false), // ()
		CHUNKS_RENDERED(true, false), // (ClientLevel)
		SHADERS_LOADED(true, false), // (ResourceManager)
		CLIENT_OPTIONS_SAVED(true, false), // (Options)
		SERVER_STRUCTURES_LOADED(false, false), // (StructureStorage)
		CLIENT_STRUCTURES_LOADED(true, false), // (StructureStorage)
		TEXTURES_RELOADED(true, false), // (TextureManager)

		;

		public final boolean clientOnly;
		public final boolean methodOnly;

		Type(boolean clientOnly, boolean methodOnly) {
			this.clientOnly = clientOnly;
			this.methodOnly = methodOnly;
		}

		public void invoke(Object... args) {
			for (var s : SCANNED.get()) {
				if (s.type() == this) {
					try {
						if (s.method().getParameterCount() == 0) {
							s.method().invoke(null);
						} else {
							s.method().invoke(null, args);
						}
					} catch (Exception ex) {
						VidLib.LOGGER.error("Failed to invoke @AutoInit method " + s.method().getDeclaringClass().getName() + "#" + s.method().getName(), ex);
					}
				}
			}
		}

		public void invoke() {
			invoke(Empty.OBJECT_ARRAY);
		}
	}

	Type[] value() default Type.DEFAULT;

	@ApiStatus.Internal
	Lazy<List<AutoMethod>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<AutoMethod>();

		AutoHelper.load(AutoInit.class, EnumSet.of(ElementType.TYPE, ElementType.METHOD, ElementType.FIELD), (source, classLoader, ad) -> {
			var types = AutoHelper.getEnumValues(ad, Type.class, "value", EnumSet.of(Type.DEFAULT));

			for (var type : types) {
				if (type == Type.DEFAULT) {
					type = Type.GAME_LOADED;
				}

				if (type.clientOnly && !PlatformHelper.CURRENT.getSide().isClient()) {
					VidLib.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
					return;
				}

				if (type.methodOnly && ad.targetType() != ElementType.METHOD) {
					VidLib.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
					return;
				}
			}

			var clazz = Class.forName(ad.clazz().getClassName(), true, classLoader);

			if (ad.targetType() == ElementType.METHOD) {
				var method = AutoHelper.getMethod(clazz, ad, classLoader);
				VidLib.LOGGER.info("Found @AutoInit method " + clazz.getName() + "#" + method.getName());

				for (var type : types) {
					list.add(new AutoMethod(type == Type.DEFAULT ? Type.GAME_LOADED : type, method));
				}
			} else {
				VidLib.LOGGER.info("Found @AutoInit class " + clazz.getName());
			}
		});

		return list;
	});
}
