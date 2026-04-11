package dev.mrbeastgaming.mods.hub.api.token;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record ProjectToken(ProjectTokenHeader header, byte[] signature, String encoded) {
	public static final Codec<ProjectToken> CODEC = Codec.STRING.comapFlatMap(string -> {
		try {
			var token = parse(string);

			if (token != null) {
				return DataResult.success(token);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return DataResult.error(() -> "Invalid token");
	}, ProjectToken::toString);

	@Nullable
	public static ProjectToken parse(@Nullable String token) throws IOException {
		int index = token == null || token.isEmpty() ? -1 : token.indexOf('.');

		if (index == -1) {
			return null;
		}

		var headerStr = token.substring(0, index);
		var signatureStr = token.substring(index + 1);

		if (headerStr.isEmpty() || signatureStr.isEmpty()) {
			return null;
		}

		var header = ProjectTokenHeader.parse(headerStr);
		var signature = StringUtils.B64_DECODER.decode(signatureStr.getBytes(StandardCharsets.ISO_8859_1));
		return new ProjectToken(header, signature, token);
	}

	@Override
	@NotNull
	public String toString() {
		return encoded;
	}
}
