package dev.latvian.mods.vidlib.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public interface JsonUtils {
	Gson GSON = new GsonBuilder().setLenient().disableHtmlEscaping().serializeNulls().create();

	static JsonElement read(Reader reader) {
		return GSON.fromJson(reader, JsonElement.class);
	}

	static JsonElement read(InputStream stream) {
		return GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
	}

	static void write(Writer writer, JsonElement json, boolean pretty) {
		var w = new JsonWriter(writer);

		if (pretty) {
			w.setIndent("\t");
		}

		GSON.toJson(json, JsonElement.class, w);
	}

	static void write(OutputStream stream, JsonElement json, boolean pretty) {
		write(new OutputStreamWriter(stream, StandardCharsets.UTF_8), json, pretty);
	}

	static String string(JsonElement json) {
		var writer = new StringWriter();
		write(writer, json, false);
		return writer.toString();
	}

	static String prettyString(JsonElement json) {
		var writer = new StringWriter();
		write(writer, json, true);
		return writer.toString();
	}
}
