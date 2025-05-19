package dev.latvian.mods.vidlib.feature.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

public interface GLDebugLog {
	enum Type {
		ERROR(GL43.GL_DEBUG_TYPE_ERROR),
		DEPRECATED_BEHAVIOR(GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR),
		UNDEFINED_BEHAVIOR(GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR),
		PORTABILITY(GL43.GL_DEBUG_TYPE_PORTABILITY),
		PERFORMANCE(GL43.GL_DEBUG_TYPE_PERFORMANCE),
		OTHER(GL43.GL_DEBUG_TYPE_OTHER),
		MARKER(GL43.GL_DEBUG_TYPE_MARKER);

		public final int id;

		Type(int id) {
			this.id = id;
		}
	}

	enum Severity {
		NOTIFICATION(GL43.GL_DEBUG_SEVERITY_NOTIFICATION),
		HIGH(GL43.GL_DEBUG_SEVERITY_HIGH),
		MEDIUM(GL43.GL_DEBUG_SEVERITY_MEDIUM),
		LOW(GL43.GL_DEBUG_SEVERITY_LOW);

		public final int id;

		Severity(int id) {
			this.id = id;
		}
	}

	int MAJOR_VERSION = GL11.glGetInteger(GL30.GL_MAJOR_VERSION);
	int MINOR_VERSION = GL11.glGetInteger(GL30.GL_MINOR_VERSION);
	boolean AVAILABLE = MAJOR_VERSION >= 4 && (MAJOR_VERSION > 4 || MINOR_VERSION >= 3);

	static void message(Object message, Type type, Severity severity) {
		if (AVAILABLE) {
			GL43.glDebugMessageInsert(GL43.GL_DEBUG_SOURCE_APPLICATION, type.id, 0, severity.id, String.valueOf(message));
		}
	}

	static void message(Object message, Severity severity) {
		message(message, Type.MARKER, severity);
	}

	static void message(Object message) {
		message(message, Type.MARKER, Severity.NOTIFICATION);
	}

	static void pushGroup(Object name) {
		if (AVAILABLE) {
			GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, String.valueOf(name));
		}
	}

	static void popGroup() {
		if (AVAILABLE) {
			GL43.glPopDebugGroup();
		}
	}
}
