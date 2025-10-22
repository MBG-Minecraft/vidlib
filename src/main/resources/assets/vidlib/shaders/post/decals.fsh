#version 410 core

uniform sampler2D InDepthSampler;
uniform sampler2D TerrainInDepthSampler;
uniform sampler2D DataSampler;
uniform int Count;
uniform mat4 InverseViewProjectionMat;
uniform float GameTime;

in vec2 texCoord;
in vec2 oneTexel;

const float sqrt3d2 = sqrt(3.0) / 2.0;

layout (location = 0) out vec4 fragColor;

uvec4 fetchRGBA8u(ivec2 tc) {
	vec4 s = texelFetch(DataSampler, tc, 0) * 255.0;
	return uvec4(round(s));
}

uint decodeUInt(int x, int y) {
	uvec4 v = fetchRGBA8u(ivec2(x, y));
	return (v.w << 24) | (v.x << 16) | (v.y << 8) | v.z;
}

float decodeFloat(int x, int y) {
	return uintBitsToFloat(decodeUInt(x, y));
}

vec4 decodeColor(int x, int y) {
	return texelFetch(DataSampler, ivec2(x, y), 0);
}

float getInside(int type, vec3 pos, float start, float end, float height, float rotation, float edges) {
	if (type == 1) {
		float degSeg = 6.283185307179586 / edges;
		float degSegd2 = degSeg / 2.0;

		float angle = mod(atan(pos.z, pos.x) + rotation + degSegd2, degSeg);
		float len = length(pos.xz);
		pos.x = cos(angle) * len;
		pos.z = sin(angle) * len;

		float m = 1.0 / cos(angle - degSegd2);
		float hs = start * sqrt3d2;
		float he = end * sqrt3d2;
		float rhs = hs * m;
		float rhe = he * m;
		float h = (len - rhs) / (rhe - rhs);

		return h >= 0.0 && h <= 1.0 ? h : -1.0;
	} else if (type == 2) {
		pos.y /= height;
		float distSq = dot(pos, pos);

		if (distSq <= end * end && distSq >= start * start) {
			return (sqrt(distSq) - start) / (end - start);
		}
	} else if (type == 3 || type == 5) {
		pos.y /= height;
		float y = abs(pos.y);

		if (y <= end) {
			float distSq = dot(pos.xz, pos.xz);
			float r = (distSq <= end * end && distSq >= start * start) ? (sqrt(distSq) - start) / (end - start) : -1.0;

			if (type == 2) {
				return r;
			} else {
				float v = (y - start) / (end - start);
				return distSq <= end * end ? max(r, v) : -1.0;
			}
		}
	} else if (type == 6) {
		if (rotation != 0.0) {
			float r = atan(pos.z, pos.x) + rotation;
			float l = length(pos.xz);
			pos.x = cos(r) * l;
			pos.z = sin(r) * l;
		}

		pos.y /= height;
		float v = max(abs(pos.x), abs(pos.z));

		if (type == 6) {
			v = max(v, abs(pos.y));
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
	if (Count == 0) {
		discard;
	}

	float depth = texture(InDepthSampler, texCoord).r;
	float terrainDepth = texture(TerrainInDepthSampler, texCoord).r;
	vec3 worldPos = clip(depth);
	vec3 terrainWorldPos = clip(terrainDepth);

	vec4 color = vec4(0.0);
	bool modified = false;

	for (int y = 0; y < 1024; y++) {
		if (y >= Count) {
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
		float edges = decodeFloat(13, y);

		vec3 pos = (terrain == 0 ? worldPos : terrainWorldPos) - decalPos;
		float inside = getInside(type, pos, start, end, height, rotation, edges);

		if (inside >= 0.0 && inside <= 1.0) {
			vec4 startColor = decodeColor(8, y);
			vec4 endColor = decodeColor(9, y);
			vec4 decalColor = mix(startColor, endColor, inside);

			if (decalColor.a > 0.0) {
				int fillHead = int(decodeUInt(10, y));
				int fillType = fillHead & 7;

				if (fillType != 0) {
					float fillSize = decodeFloat(11, y);
					float fillThickness = decodeFloat(12, y);

					if (fillType == 1) {
						vec3 g = pos + fillThickness * 0.5;

						if (rotation != 0.0) {
							float r = atan(g.z, g.x) + rotation;
							float l = length(g.xz);
							g.x = cos(r) * l;
							g.z = sin(r) * l;
						}

						if (mod(g.x, fillSize) < fillThickness || mod(g.y, fillSize) < fillThickness || mod(g.z, fillSize) < fillThickness) {
							color = blend(color, decalColor);
							modified = true;
						}

						continue;
					} else if (fillType == 2 || fillType == 3) {
						float fillThickness = decodeFloat(12, y);
						vec3 g = pos + fillThickness * 0.5;

						if (rotation != 0.0) {
							float r = atan(g.z, g.x) + rotation;
							float l = length(g.xz);
							g.x = cos(r) * l;
							g.z = sin(r) * l;
						}

						float time = (fillType == 3) ? (GameTime * 1200.0 * fillSize) : 0.0;

						if (mod(g.x + g.y + g.z + time, fillSize) < fillThickness) {
							color = blend(color, decalColor);
							modified = true;
						}

						continue;
					}
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
