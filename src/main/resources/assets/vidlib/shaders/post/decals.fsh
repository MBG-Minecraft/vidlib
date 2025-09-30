#version 330

uniform sampler2D InDepthSampler;
uniform sampler2D TerrainInDepthSampler;
uniform sampler2D DecalsSampler;
uniform int DecalCount;
uniform mat4 InverseViewProjectionMat;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uvec4 fetchRGBA8u(ivec2 tc) {
	vec4 s = texelFetch(DecalsSampler, tc, 0) * 255.0;
	return uvec4(round(s));
}

uint decodeUInt(int x, int y) {
	uvec4 v = fetchRGBA8u(ivec2(x, y));
	// assemble as unsigned to avoid UB
	return (v.w << 24) | (v.x << 16) | (v.y << 8) | v.z;
}

float decodeFloat(int x, int y) {
	return uintBitsToFloat(decodeUInt(x, y));
}

vec4 decodeColor(int x, int y) {
	return texelFetch(DecalsSampler, ivec2(x, y), 0);
}

float getInside(int type, vec3 diff, float start, float end, float height, float rotation) {
	if (type == 1) {
		diff.y /= height;
		float distSq = dot(diff, diff);

		if (distSq <= end * end && distSq >= start * start) {
			return (sqrt(distSq) - start) / (end - start);
		}
	} else if (type == 2 || type == 3) {
		diff.y /= height;
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
		diff.y /= height;
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

vec3 clip(float depth) {
	vec4 clipPos;
	clipPos.xy = texCoord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	return homogenousPos.xyz / homogenousPos.w;
}

void main() {
	if (DecalCount == 0) {
		discard;
	}

	float depth = texture(InDepthSampler, texCoord).r;
	float terrainDepth = texture(TerrainInDepthSampler, texCoord).r;
	vec3 worldPos = clip(depth);
	vec3 terrainWorldPos = clip(terrainDepth);

	vec4 color = vec4(0.0);
	bool modified = false;

	for (int y = 0; y < 1024; y++) {
		if (y >= DecalCount) {
			break;
		}

		int head = int(decodeUInt(0, y));
		int type = head & 7;
		int terrain = head & 16;

		if (type == 0 || (terrain != 0 && terrainDepth > depth)) {
			continue;
		}

		vec3 decalPos = vec3(decodeFloat(1, y), decodeFloat(2, y), decodeFloat(3, y));
		float start = decodeFloat(4, y);
		float end = decodeFloat(5, y);
		float height = decodeFloat(6, y);
		float rotation = decodeFloat(7, y);

		vec3 diff = (terrain == 0 ? worldPos : terrainWorldPos) - decalPos;
		float inside = getInside(type, diff, start, end, height, rotation);

		if (inside >= 0.0 && inside <= 1.0) {
			vec4 startColor = decodeColor(8, y);
			vec4 endColor = decodeColor(9, y);
			vec4 decalColor = mix(startColor, endColor, inside);

			if (decalColor.a > 0.0) {
				float grid = decodeFloat(10, y);

				if (grid > 0.0) {
					float thickness = decodeFloat(11, y);
					vec3 g = abs(diff) + thickness * 0.5;

					if (mod(g.x, grid) < thickness || mod(g.y, grid) < thickness || mod(g.z, grid) < thickness) {
						color = blend(color, decalColor);
						modified = true;
					}

					continue;
				}

				color = blend(color, decalColor);
				modified = true;
			}
		}
	}

	if (modified) {
		fragColor = color;
	} else {
		discard;
	}
}
