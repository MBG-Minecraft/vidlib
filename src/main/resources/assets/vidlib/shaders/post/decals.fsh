#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D DecalsSampler;
uniform int DecalCount;
uniform mat4 InverseViewProjectionMat;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

int decodeInt(int x, int y) {
	ivec4 v = ivec4(texelFetch(DecalsSampler, ivec2(x, y), 0) * 255);
	return (v.a << 24) | (v.r << 16) | (v.g << 8) | v.b;
}

float decodeFloat(int x, int y) {
	return intBitsToFloat(decodeInt(x, y));
}

vec4 decodeColor(int x, int y) {
	return vec4(texelFetch(DecalsSampler, ivec2(x, y), 0));
}

float getInside(int type, vec3 diff, float start, float end, float rotation) {
	if (type == 1) {
		float distSq = dot(diff, diff);

		if (distSq <= end * end && distSq >= start * start) {
			return (sqrt(distSq) - start) / (end - start);
		}
	} else if (type == 2 || type == 3) {
		float y = abs(diff.y);

		if (y <= end) {
			float distSq = dot(diff.xz, diff.xz);
			float r = (distSq <= end * end && distSq >= start * start) ? (sqrt(distSq) - start) / (end - start) : -1.0;

			if (type == 2) {
				return r;
			} else {
				float v = (y - start) / (end - start);
				return distSq <= end * end ? max(r, v) : -1.0;
			}
		}
	} else if (type == 4 || type == 5) {
		float v = max(abs(diff.x), abs(diff.z));

		if (type == 5) {
			v = max(v, abs(diff.y));
		}

		if (v >= start && v <= end) {
			return (v - start) / (end - start);
		}
	}

	return -1.0;
}

vec4 blend(vec4 src, vec4 dst) {
	vec3 c = src.rgb * src.a + dst.rgb * (1.0 - src.a);
	float a = src.a + dst.a * (1.0 - src.a);
	return vec4(c, a);
}

void main() {
	if (DecalCount == 0) {
		discard;
	}

	float depth = texture(InDepthSampler, texCoord).r;
	vec4 clipPos;
	clipPos.xy = texCoord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	vec3 worldPos = homogenousPos.xyz / homogenousPos.w;

	vec4 color = vec4(0.0, 0.0, 0.0, 0.0);

	for (int y = 0; y < DecalCount; y++) {
		int head = decodeInt(0, y);
		int type = head & 7;

		if (type == 0) {
			continue;
		}

		vec3 decalPos = vec3(decodeFloat(1, y), decodeFloat(2, y), decodeFloat(3, y));
		float start = decodeFloat(4, y);
		float end = decodeFloat(5, y);
		float rotation = decodeFloat(6, y);
		vec3 diff = worldPos - decalPos;
		float inside = getInside(type, diff, start, end, rotation);

		if (inside >= 0.0 && inside <= 1.0) {
			vec4 startColor = decodeColor(8, y);
			vec4 endColor = decodeColor(9, y);
			vec4 decalColor = mix(startColor, endColor, inside);

			if (decalColor.a > 0.0) {
				float grid = decodeFloat(7, y);

				if (grid > 0.0) {
					float thickness = decodeFloat(10, y);
					vec3 g = abs(diff) + thickness * 0.5;

					if (mod(g.x, grid) < thickness || mod(g.y, grid) < thickness || mod(g.z, grid) < thickness) {
						color = blend(color, decalColor);
					}

					continue;
				}

				color = blend(color, decalColor);
			}
		}
	}

	if (color.a > 0.0) {
		fragColor = color;
	} else {
		discard;
	}
}
