package dev.latvian.mods.vidlib.feature.auto;

public interface AutoCallback {
	void accept(String source, ClassLoader classLoader, ScannedAnnotation ad) throws Exception;
}
