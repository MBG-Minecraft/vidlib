package dev.latvian.mods.vidlib.feature.auto;

import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.Map;

public record ScannedAnnotation(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {
}
