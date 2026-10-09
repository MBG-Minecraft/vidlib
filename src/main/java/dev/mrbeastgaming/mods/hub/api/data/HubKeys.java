package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.StringUtils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class HubKeys {
	public static final HubKeys NONE = new HubKeys("", new byte[0]);

	public static HubKeys of(String algorithm, byte[] publicKey) {
		if (algorithm.isEmpty() && publicKey.length == 0) {
			return NONE;
		}

		return new HubKeys(algorithm, publicKey);
	}

	public static HubKeys of(Key key) {
		return of(key.getAlgorithm(), key.getEncoded());
	}

	public static final Codec<HubKeys> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("algorithm", NONE.algorithm).forGetter(HubKeys::algorithm),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("public", NONE.publicKey).forGetter(HubKeys::publicKey)
	).apply(instance, HubKeys::of));

	private final String algorithm;
	private final byte[] publicKey;
	private Optional<PublicKey> publicKeyInstance;

	private HubKeys(String algorithm, byte[] publicKey) {
		this.algorithm = algorithm;
		this.publicKey = publicKey;
		this.publicKeyInstance = null;
	}

	public synchronized boolean verify(byte[] data, byte[] signature) {
		if (isNone()) {
			return false;
		}

		try {
			if (publicKeyInstance == null) {
				try {
					var keyFactory = KeyFactory.getInstance(algorithm);
					publicKeyInstance = Optional.ofNullable(keyFactory.generatePublic(new X509EncodedKeySpec(publicKey)));
				} catch (Exception ex) {
					publicKeyInstance = Optional.empty();
				}
			}

			var instance = publicKeyInstance.orElse(null);

			if (instance == null) {
				return false;
			}

			var sig = Signature.getInstance(algorithm);
			sig.initVerify(instance);
			sig.update(data);
			return sig.verify(signature);
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean isNone() {
		return algorithm.isEmpty() && publicKey.length == 0;
	}

	public String algorithm() {
		return algorithm;
	}

	public byte[] publicKey() {
		return publicKey;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (HubKeys) obj;
		return Objects.equals(this.algorithm, that.algorithm) && Arrays.equals(this.publicKey, that.publicKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(algorithm, Arrays.hashCode(publicKey));
	}

	@Override
	public String toString() {
		return "HubKeys[" + algorithm + " " + StringUtils.toHex(publicKey) + ']';
	}
}
