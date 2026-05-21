package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public record HubKeyData(
	String algorithm,
	byte[] publicKey
) {
	public static final Codec<HubKeyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("algorithm", "").forGetter(HubKeyData::algorithm),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("public", new byte[0]).forGetter(HubKeyData::publicKey)
	).apply(instance, HubKeyData::new));

	public PublicKey createPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		var keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
	}
}
